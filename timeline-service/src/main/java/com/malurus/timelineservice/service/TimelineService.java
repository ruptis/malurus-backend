package com.malurus.timelineservice.service;

import com.malurus.timelineservice.client.UserServiceClient;
import com.malurus.timelineservice.client.PostServiceClient;
import com.malurus.timelineservice.constants.EntityName;
import com.malurus.timelineservice.dto.response.UserResponse;
import com.malurus.timelineservice.dto.response.PostResponse;
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

    private final UserServiceClient userServiceClient;
    private final PostServiceClient postServiceClient;
    private final CacheService cacheService;

    public List<PostResponse> getUserTimelineForLoggedInUser(String loggedInUser, PageRequest page) {
        return getUserTimeline(userServiceClient.getAuthProfile(loggedInUser), page);
    }

    public List<PostResponse> getUserTimelineForAnotherInUser(String profileId, PageRequest page) {
        return getUserTimeline(userServiceClient.getProfileById(profileId), page);
    }

    public List<PostResponse> getRepliesUserTimelineForLoggedInUser(String loggedInUser, PageRequest page) {
        return getRepliesUserTimeline(userServiceClient.getAuthProfile(loggedInUser), page);
    }

    public List<PostResponse> getRepliesUserTimelineForAnotherInUser(String profileId, PageRequest page) {
        return getRepliesUserTimeline(userServiceClient.getProfileById(profileId), page);
    }

    public List<PostResponse> getHomeTimelineForLoggedInUser(String loggedInUser, PageRequest page) {
        UserResponse profile = userServiceClient.getAuthProfile(loggedInUser);
        List<PostResponse> tweets = getEntityHomeTimeline(profile, page, TWEETS, postServiceClient::getAllTweetsForUser, postServiceClient::getTweet);
        List<PostResponse> retweets = getEntityHomeTimeline(profile, page, RETWEETS, postServiceClient::getAllRetweetsForUser, postServiceClient::getRetweet);
        return mergeTwoSortedLists(tweets, retweets);
    }

    private List<PostResponse> getUserTimeline(UserResponse profile, PageRequest page) {
        List<PostResponse> tweets = getEntityUserTimeline(profile, page, TWEETS, postServiceClient::getAllTweetsForUser, postServiceClient::getTweet);
        List<PostResponse> retweets = getEntityUserTimeline(profile, page, RETWEETS, postServiceClient::getAllRetweetsForUser, postServiceClient::getRetweet);
        return mergeTwoSortedLists(tweets, retweets);
    }

    private List<PostResponse> getRepliesUserTimeline(UserResponse profile, PageRequest page) {
        List<PostResponse> replies = getEntityUserTimeline(profile, page, REPLIES, postServiceClient::getAllRepliesForUser, postServiceClient::getReply);
        List<PostResponse> retweets = getEntityUserTimeline(profile, page, RETWEETS, postServiceClient::getAllRetweetsForUser, postServiceClient::getRetweet);
        return mergeTwoSortedLists(replies, retweets);
    }

    private List<PostResponse> getEntityUserTimeline(
            UserResponse profile,
            PageRequest page,
            EntityName entityName,
            Function3<String, Integer, Integer, List<PostResponse>> obtainEntitiesFromDbFunc,
            BiFunction<Long, String, PostResponse> mapFunc
    ) {
        String timelineKey = TimelineCachePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName()) + profile.getProfileId();
        int seenNumberOfEntities = page.getPageNumber() * (page.getPageSize() / 2);

        List<PostResponse> userTimeline = mapIdsToEntities(cacheService.getTimelineFromCache(timelineKey), profile.getEmail(), mapFunc);
        log.info("{} userTimeline received from cache", entityName.getName());

        if (userTimeline == null || (userTimeline.size() <= seenNumberOfEntities && userTimeline.size() > 0)) {
            log.info("{} userTimeline is null or its size is too small", entityName.getName());
            userTimeline = obtainEntitiesFromDbFunc.apply(profile.getProfileId(), 0, seenNumberOfEntities+100);

            cacheService.cacheTimeline(mapEntitiesToIds(userTimeline), timelineKey);
            log.info("{} userTimeline has been cached with size {}", entityName.getName(), userTimeline.size());
        }

        if (seenNumberOfEntities < userTimeline.size()) {
            userTimeline = userTimeline.subList(seenNumberOfEntities, Math.min(seenNumberOfEntities + (page.getPageSize() / 2), userTimeline.size()));
        }
        return userTimeline;
    }

    private List<PostResponse> getEntityHomeTimeline(
            UserResponse profile,
            PageRequest page,
            EntityName entityName,
            Function3<String, Integer, Integer, List<PostResponse>> obtainEntitiesFromDbFunc,
            BiFunction<Long, String, PostResponse> mapFunc
    ) {
        String timelineKey = TimelineCachePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName()) + profile.getProfileId();
        int seenNumberOfEntities = page.getPageNumber() * (page.getPageSize() / 2);

        List<PostResponse> homeTimeline = mapIdsToEntities(cacheService.getTimelineFromCache(timelineKey), profile.getEmail(), mapFunc);
        log.info("{} homeTimeline received from cache", entityName.getName());

        if (homeTimeline == null || (homeTimeline.size() <= seenNumberOfEntities && homeTimeline.size() > 0)) {
            log.info("{} homeTimeline is null or its size is too small", entityName.getName());

            List<List<PostResponse>> lists = new LinkedList<>();
            for (UserResponse followee : userServiceClient.getFollowees(profile.getProfileId())) {
                if (followee.getFollowers() < 10000) {
                    lists.add(getEntityUserTimeline(
                            followee,
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

        for (UserResponse celebrity : userServiceClient.getFolloweesCelebrities(profile.getProfileId())) {
            homeTimeline.addAll(getEntityUserTimeline(celebrity, page, entityName, obtainEntitiesFromDbFunc, mapFunc));
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
        int i = 0, j = 0;
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
