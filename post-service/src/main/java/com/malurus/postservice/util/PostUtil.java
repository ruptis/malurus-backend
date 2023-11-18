package com.malurus.postservice.util;

import com.malurus.postservice.constant.EntityName;
import com.malurus.postservice.constant.Operation;
import com.malurus.postservice.dto.message.EntityMessage;
import com.malurus.postservice.entity.Post;
import com.malurus.postservice.exception.ActionNotAllowedException;
import com.malurus.postservice.repository.LikeRepository;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.repository.ViewRepository;
import com.malurus.postservice.service.KafkaProducerService;
import com.malurus.postservice.service.MessageSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.malurus.postservice.constant.CacheName.*;
import static com.malurus.postservice.constant.EntityName.*;
import static com.malurus.postservice.constant.Operation.DELETE;
import static com.malurus.postservice.constant.TopicName.HOME_TIMELINE_TOPIC;
import static com.malurus.postservice.constant.TopicName.USER_TIMELINE_TOPIC;

@RequiredArgsConstructor
@Component
public class PostUtil {

    public enum EvictionStrategy {CACHE_ONLY, WITH_TIMELINE}

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final ViewRepository viewRepository;
    private final KafkaProducerService kafkaProducerService;
    private final MessageSourceService messageSourceService;
    private final CacheManager cacheManager;

    public int countRepliesForPost(Long postId) {
        return postRepository.countAllByReplyToId(postId);
    }

    public int countLikesForPost(Long postId) {
        return likeRepository.countAllByParentPostId(postId);
    }

    public int countRepostsForPost(Long postId) {
        return postRepository.countAllByRepostToId(postId);
    }

    public int countViewsForPost(Long postId) {
        return viewRepository.countAllByParentPostId(postId);
    }

    public boolean isEntityOwnedByLoggedInUser(Post entity, String loggedInUser) {
        if (!isPostBelongsToUser(entity, loggedInUser)) {
            throw new ActionNotAllowedException(
                    messageSourceService.generateMessage("error.action_not_allowed")
            );
        }
        return true;
    }

    public boolean isPostBelongsToUser(Post entity, String userId) {
        return userId.equals(entity.getUserId());
    }

    public boolean isPostRepostedByUser(Long repostToId, String userId) {
        return postRepository.findByRepostToIdAndUserId(repostToId, userId).isPresent();
    }

    public boolean isPostLikedByUser(Long parentPostId, String userId) {
        return likeRepository.findByParentPostIdAndUserId(parentPostId, userId).isPresent();
    }

    public void sendMessageToKafka(String topic, Post entity, EntityName entityName, Operation operation) {
        EntityMessage entityMessage = EntityMessage.valueOf(entity.getId(), entity.getUserId(), entityName, operation);
        kafkaProducerService.send(entityMessage, topic);
    }

    public void sendMessageWithPost(Post post, Operation operation) {
        sendMessageToKafka(USER_TIMELINE_TOPIC, post, POSTS, operation);
        sendMessageToKafka(HOME_TIMELINE_TOPIC, post, POSTS, operation);
    }

    public void sendMessageWithRepost(Post repost, Operation operation) {
        sendMessageToKafka(USER_TIMELINE_TOPIC, repost, REPOSTS, operation);
        sendMessageToKafka(HOME_TIMELINE_TOPIC, repost, REPOSTS, operation);
    }

    public void sendMessageWithReply(Post reply, Operation operation) {
        sendMessageToKafka(USER_TIMELINE_TOPIC, reply, REPLIES, operation);
    }

    public void evictEntityFromCache(Long entityId, String cacheName) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).evictIfPresent(Long.toString(entityId));
    }

    public void evictAllEntityRelationsFromCache(Post entity, EvictionStrategy strategy) {
        Set<Post> rePosts = entity.getReposts();
        Set<Post> replies = entity.getReplies();
        List<Post> quotedPosts = postRepository.findAllByQuoteToId(entity.getId());

        evictEntitiesFromCache(rePosts, REPOSTS_CACHE_NAME);
        evictEntitiesFromCache(replies, REPLIES_CACHE_NAME);
        evictEntitiesFromCache(quotedPosts, POSTS_CACHE_NAME);

        if (Objects.requireNonNull(strategy) == EvictionStrategy.WITH_TIMELINE) {
            evictEntitiesFromTimelineCache(rePosts, this::sendMessageWithRepost);
            evictEntitiesFromTimelineCache(replies, this::sendMessageWithReply);
            evictEntitiesFromTimelineCache(quotedPosts, this::sendMessageWithPost);
        }
    }

    private void evictEntitiesFromCache(Iterable<Post> entities, String cacheName) {
        for (Post entity : entities) {
            evictEntityFromCache(entity.getId(), cacheName);
        }
    }

    private void evictEntitiesFromTimelineCache(Iterable<Post> entities, BiConsumer<Post, Operation> evictionConsumer) {
        for (Post entity : entities) {
            evictionConsumer.accept(entity, DELETE);
        }
    }
}
