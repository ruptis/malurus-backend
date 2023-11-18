package com.malurus.fanoutservice.service;

import com.malurus.fanoutservice.client.SocialGraphServiceClient;
import com.malurus.fanoutservice.constants.Operation;
import com.malurus.fanoutservice.dto.message.EntityMessage;
import com.malurus.fanoutservice.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FanoutService {

    @AllArgsConstructor
    @Getter
    @ToString
    private enum TimelineCachePrefix {
        USER_TIMELINE_PREFIX("%s_user_timeline:"),
        HOME_TIMELINE_PREFIX("%s_home_timeline:");
        private final String prefix;
    }

    private final SocialGraphServiceClient socialGraphServiceClient;
    private final CacheService cacheService;

    public void processMessageForUserTimeline(EntityMessage entityMessage) {
        final Long entityId = entityMessage.entityId();
        final String timelineKey = TimelineCachePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityMessage.entityName()) + entityMessage.userId();

        final Operation operation = Operation.valueOf(entityMessage.operation());
        if (operation == Operation.ADD) {
            addEntityToTimeline(entityId, timelineKey);
        } else if (operation == Operation.DELETE) {
            deleteEntityFromTimeline(entityId, timelineKey);
        }
    }

    public void processMessageForHomeTimeline(EntityMessage entityMessage) {
        if (Boolean.TRUE.equals(socialGraphServiceClient.isCelebrity(entityMessage.userId()))) return;
        final Long entityId = entityMessage.entityId();
        List<String> followers = socialGraphServiceClient.getFollowers(entityMessage.userId()).stream().map(UserResponse::getId).toList();
        String prefix = TimelineCachePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityMessage.entityName());

        final Operation operation = Operation.valueOf(entityMessage.operation());
        for (String followerId : followers) {
            final String timelineKey = prefix + followerId;

            if (operation == Operation.ADD) {
                addEntityToTimeline(entityId, timelineKey);
            } else if (operation == Operation.DELETE) {
                deleteEntityFromTimeline(entityId, timelineKey);
            }
        }

    }

    private void addEntityToTimeline(Long entityId, String timelineKey) {
        List<Long> timeline = cacheService.getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.add(0, entityId);
            cacheService.cacheTimeline(timeline, timelineKey);
        }
    }

    private void deleteEntityFromTimeline(Long entityId, String timelineKey) {
        List<Long> timeline = cacheService.getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.removeIf(id -> id.equals(entityId));
            cacheService.cacheTimeline(timeline, timelineKey);
        }
    }
}
