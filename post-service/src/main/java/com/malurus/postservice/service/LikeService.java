package com.malurus.postservice.service;

import com.malurus.postservice.mapper.LikeMapper;
import com.malurus.postservice.repository.LikeRepository;
import com.malurus.postservice.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.malurus.postservice.constant.CacheName.POSTS_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final LikeMapper likeMapper;
    private final MessageSourceService messageSourceService;
    private final CacheManager cacheManager;

    public void likePost(Long postId, String loggedInUser) {
        postRepository.findById(postId)
                .map(post -> likeMapper.toEntity(post, loggedInUser))
                .map(likeRepository::saveAndFlush)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", postId)
                ));
        Objects.requireNonNull(cacheManager.getCache(POSTS_CACHE_NAME)).evictIfPresent(postId);
    }

    public void unlikePost(Long postId, String loggedInUser) {
        likeRepository.findByParentPostIdAndUserId(postId, loggedInUser)
                .ifPresentOrElse(likeRepository::delete, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", postId)
                    );
                });
        Objects.requireNonNull(cacheManager.getCache(POSTS_CACHE_NAME)).evictIfPresent(postId);
    }
}
