package com.malurus.socialgraphservice.service;


import com.malurus.socialgraphservice.dto.response.UserResponse;
import com.malurus.socialgraphservice.entity.Relationship;
import com.malurus.socialgraphservice.entity.User;
import com.malurus.socialgraphservice.exception.EntityNotFoundException;
import com.malurus.socialgraphservice.mapper.UserMapper;
import com.malurus.socialgraphservice.repository.RelationshipRepository;
import com.malurus.socialgraphservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.malurus.socialgraphservice.constant.CacheName.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialGraphService {
    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;

    public void addUser(String userId) {
        userRepository.save(User.builder()
                .id(userId)
                .build());
    }

    public boolean follow(String followerId, String followeeId) {
        User follower = getUser(followerId);
        User followee = getUser(followeeId);
        if (!isFollowed(follower, followee)) {
            relationshipRepository.save(Relationship.builder()
                    .follower(follower)
                    .followee(followee)
                    .build());
            evictCache(followerId, followeeId);
            log.info("User {} followed user {}", followerId, followeeId);
        }
        return true;
    }

    public boolean unfollow(String followerId, String followeeId) {
        User follower = getUser(followerId);
        User followee = getUser(followeeId);
        if (isFollowed(follower, followee)) {
            relationshipRepository.deleteRelationshipByFollowerAndFollowee(follower, followee);
            evictCache(followerId, followeeId);
            log.info("User {} unfollowed user {}", followerId, followeeId);
        }
        return true;
    }

    @Cacheable(cacheNames = FOLLOWERS_CACHE, key = "#p0")
    public List<UserResponse> getFollowers(String followeeId) {
        return relationshipRepository.findAllByFolloweeId(followeeId).stream()
                .map(Relationship::getFollower)
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = FOLLOWEES_CACHE, key = "#p0")
    public List<UserResponse> getFollowees(String followerId) {
        return relationshipRepository.findAllByFollowerId(followerId).stream()
                .map(Relationship::getFollowee)
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = FOLLOWEES_CELEBRITIES_CACHE, key = "#p0")
    public List<UserResponse> getFolloweesCelebrities(String followerId) {
        return getFollowees(followerId).stream()
                .filter(UserResponse::isCelebrity)
                .collect(Collectors.toList());
    }

    public boolean isFollowed(String followerId, String followeeId) {
        return relationshipRepository.existsRelationshipByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    public boolean isCelebrity(String id) {
        return userRepository.findById(id).map(User::isCelebrity).orElse(false);
    }

    private User getUser(String followerId) {
        return userRepository.findById(followerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private boolean isFollowed(User follower, User followee) {
        return relationshipRepository.existsRelationshipByFollowerAndFollowee(follower, followee);
    }

    private void evictCache(String followerId, String followeeId) {
        Objects.requireNonNull(cacheManager.getCache(FOLLOWERS_CACHE)).evictIfPresent(followeeId);
        Objects.requireNonNull(cacheManager.getCache(FOLLOWEES_CACHE)).evictIfPresent(followerId);
        Objects.requireNonNull(cacheManager.getCache(FOLLOWEES_CELEBRITIES_CACHE)).evictIfPresent(followerId);
    }
}

