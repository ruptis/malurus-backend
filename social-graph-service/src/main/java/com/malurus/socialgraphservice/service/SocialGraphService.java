package com.malurus.socialgraphservice.service;


import com.malurus.socialgraphservice.entity.Relationship;
import com.malurus.socialgraphservice.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.malurus.socialgraphservice.constant.CacheName.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialGraphService {
    private final RelationshipRepository relationshipRepository;
    private final CacheManager cacheManager;

    public boolean follow(String followerId, String followeeId) {
        if (!isFollowed(followerId, followeeId)) {
            relationshipRepository.save(Relationship.builder()
                    .followerId(followerId)
                    .followeeId(followeeId)
                    .followDateTime(LocalDateTime.now())
                    .build());
        }
        return true;
    }

    public boolean unfollow(String followerId, String followeeId) {
        if (isFollowed(followerId, followeeId)) {
            Objects.requireNonNull(cacheManager.getCache(FOLLOWERS_CACHE)).evictIfPresent(followeeId);
            Objects.requireNonNull(cacheManager.getCache(FOLLOWEES_CACHE)).evictIfPresent(followerId);
            Objects.requireNonNull(cacheManager.getCache(FOLLOWEES_CELEBRITIES_CACHE)).evictIfPresent(followerId);
            return relationshipRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId).isPresent();
        }
        return true;
    }

    public Integer getFollowersCount(String followeeId) {
        return relationshipRepository.countAllByFolloweeId(followeeId);
    }

    public Integer getFolloweesCount(String followerId) {
        return relationshipRepository.countAllByFollowerId(followerId);
    }

    @Cacheable(cacheNames = FOLLOWERS_CACHE, key = "#p0")
    public List<String> getFollowers(String followeeId) {
        return relationshipRepository.findAllByFolloweeId(followeeId).stream()
                .map(Relationship::getFollowerId)
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = FOLLOWEES_CACHE, key = "#p0")
    public List<String> getFollowees(String followerId) {
        return relationshipRepository.findAllByFollowerId(followerId).stream()
                .map(Relationship::getFolloweeId)
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = FOLLOWEES_CELEBRITIES_CACHE, key = "#p0")
    public List<String> getFolloweesCelebrities(String followerId) {
        return getFollowees(followerId).stream()
                .filter(this::isCelebrity)
                .collect(Collectors.toList());
    }

    public boolean isFollowed(String followerId, String followeeId) {
        return relationshipRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    private boolean isCelebrity(String id) {
        return getFollowersCount(id) > 10000;
    }
}

