package com.malurus.timelineservice.service;

import com.malurus.timelineservice.client.SocialGraphServiceClient;
import com.malurus.timelineservice.client.PostServiceClient;
import com.malurus.timelineservice.constants.EntityName;
import com.malurus.timelineservice.dto.response.PostResponse;
import com.malurus.timelineservice.dto.response.UserResponse;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.function.Function3;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.malurus.timelineservice.constants.EntityName.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

    @Getter
    @AllArgsConstructor
    @ToString
    private enum TimelineCachePrefix {
        USER_TIMELINE_PREFIX("%s_user_timeline:"),
        HOME_TIMELINE_PREFIX("%s_home_timeline:");

        private final String prefix;
    }

    private final SocialGraphServiceClient socialGraphServiceClient;
    private final PostServiceClient postServiceClient;
    private final CacheService cacheService;

    public List<PostResponse> getUserTimeline(String userId, PageRequest page) {
        List<PostResponse> posts = getEntityUserTimeline(userId, page, POSTS, postServiceClient::getAllPostsForUser, postServiceClient::getPost);
        List<PostResponse> reposts = getEntityUserTimeline(userId, page, REPOSTS, postServiceClient::getAllRepostsForUser, postServiceClient::getRepost);
        return mergeTwoSortedLists(posts, reposts);
    }

    public List<PostResponse> getRepliesUserTimeline(String userId, PageRequest page) {
        List<PostResponse> replies = getEntityUserTimeline(userId, page, REPLIES, postServiceClient::getAllRepliesForUser, postServiceClient::getReply);
        List<PostResponse> reposts = getEntityUserTimeline(userId, page, REPOSTS, postServiceClient::getAllRepostsForUser, postServiceClient::getRepost);
        return mergeTwoSortedLists(replies, reposts);
    }

    public List<PostResponse> getHomeTimeline(String loggedInUser, PageRequest page) {
        List<PostResponse> tweets = getEntityHomeTimeline(loggedInUser, page, POSTS, postServiceClient::getAllPostsForUser, postServiceClient::getPost);
        List<PostResponse> retweets = getEntityHomeTimeline(loggedInUser, page, REPOSTS, postServiceClient::getAllRepostsForUser, postServiceClient::getRepost);
        return mergeTwoSortedLists(tweets, retweets);
    }

    private List<PostResponse> getEntityUserTimeline(
            String userId,
            PageRequest page,
            EntityName entityName,
            Function3<String, Integer, Integer, List<PostResponse>> obtainEntitiesFromDbFunc,
            BiFunction<Long, String, PostResponse> mapFunc
    ) {
        String timelineKey = TimelineCachePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName()) + userId;
        int seenNumberOfEntities = page.getPageNumber() * (page.getPageSize() / 2);

        List<PostResponse> userTimeline = mapIdsToEntities(cacheService.getTimelineFromCache(timelineKey), userId, mapFunc);
        log.info("{} userTimeline received from cache", entityName.getName());

        if (userTimeline == null || (userTimeline.size() <= seenNumberOfEntities && !userTimeline.isEmpty())) {
            log.info("{} userTimeline is null or its size is too small", entityName.getName());
            userTimeline = obtainEntitiesFromDbFunc.apply(userId, 0, seenNumberOfEntities+100);

            cacheService.cacheTimeline(mapEntitiesToIds(userTimeline), timelineKey);
            log.info("{} userTimeline has been cached with size {}", entityName.getName(), userTimeline.size());
        }

        if (seenNumberOfEntities < userTimeline.size()) {
            userTimeline = userTimeline.subList(seenNumberOfEntities, Math.min(seenNumberOfEntities + (page.getPageSize() / 2), userTimeline.size()));
        }
        return userTimeline;
    }

    private List<PostResponse> getEntityHomeTimeline(
            String userId,
            PageRequest page,
            EntityName entityName,
            Function3<String, Integer, Integer, List<PostResponse>> obtainEntitiesFromDbFunc,
            BiFunction<Long, String, PostResponse> mapFunc
    ) {
        String timelineKey = TimelineCachePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName()) + userId;
        int seenNumberOfEntities = page.getPageNumber() * (page.getPageSize() / 2);

        List<PostResponse> homeTimeline = mapIdsToEntities(cacheService.getTimelineFromCache(timelineKey), userId, mapFunc);
        log.info("{} homeTimeline received from cache", entityName.getName());

        if (homeTimeline == null || (homeTimeline.size() <= seenNumberOfEntities && !homeTimeline.isEmpty())) {
            log.info("{} homeTimeline is null or its size is too small", entityName.getName());

            List<List<PostResponse>> lists = new LinkedList<>();
            for (UserResponse followee : socialGraphServiceClient.getFollowees(userId)) {
                if (!followee.isCelebrity()) {
                    lists.add(getEntityUserTimeline(
                            followee.getId(),
                            PageRequest.of(0, seenNumberOfEntities+page.getPageSize()),
                            entityName,
                            obtainEntitiesFromDbFunc,
                            mapFunc
                    ));
                }
            }
            if (!lists.isEmpty()) {
                homeTimeline = mergeKSortedLists(lists, 0, lists.size() - 1);
                cacheService.cacheTimeline(mapEntitiesToIds(homeTimeline), timelineKey);
                log.info("{} homeTimeline has been cached with size {}", entityName.getName(), homeTimeline.size());
            } else {
                homeTimeline = new LinkedList<>();
            }
        }

        if (seenNumberOfEntities < homeTimeline.size()) {
            homeTimeline = homeTimeline.subList(seenNumberOfEntities, Math.min(seenNumberOfEntities + (page.getPageSize() / 2), homeTimeline.size()));
        }

        for (UserResponse celebrity : socialGraphServiceClient.getFolloweesCelebrities(userId)) {
            homeTimeline.addAll(getEntityUserTimeline(celebrity.getId(), page, entityName, obtainEntitiesFromDbFunc, mapFunc));
        }

        homeTimeline.sort((a,b) -> b.getCreationDate().compareTo(a.getCreationDate()));
        return homeTimeline;
    }

    private List<PostResponse> mergeKSortedLists(List<List<PostResponse>> lists, int l, int r) {
        if (l > r) {
            throw new IllegalArgumentException("left pointer should not be greater than right pointer");
        } else if (l == r) {
            return lists.get(l);
        }

        int mid = l + (r - l) / 2;
        List<PostResponse> left = mergeKSortedLists(lists, l, mid);
        List<PostResponse> right = mergeKSortedLists(lists, mid + 1, r);
        return mergeTwoSortedLists(left, right);
    }

    private List<PostResponse> mergeTwoSortedLists(List<PostResponse> list1, List<PostResponse> list2) {
        List<PostResponse> res = new LinkedList<>();
        int i = 0;
        int j = 0;
        while (i < list1.size() && j < list2.size()) {
            if (list1.get(i).getCreationDate().isAfter(list2.get(j).getCreationDate())) {
                res.add(list1.get(i++));
            } else {
                res.add(list2.get(j++));
            }
        }

        while (i < list1.size()) {
            res.add(list1.get(i++));
        }
        while (j < list2.size()) {
            res.add(list2.get(j++));
        }

        return res;
    }

    private List<PostResponse> mapIdsToEntities(List<Long> idList, String loggedInUser, BiFunction<Long, String, PostResponse> mapFunc) {
        if (idList == null) {
            return null;
        }
        return idList.stream()
                .filter(Objects::nonNull)
                .map(id -> mapFunc.apply(id, loggedInUser))
                .collect(Collectors.toList());
    }

    private List<Long> mapEntitiesToIds(@NonNull List<PostResponse> entities) {
        return entities.stream()
                .map(PostResponse::getId)
                .collect(Collectors.toList());
    }
}
