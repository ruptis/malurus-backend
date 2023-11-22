package com.malurus.postservice.service;

import com.malurus.postservice.entity.Post;
import com.malurus.postservice.exception.CreateEntityException;
import com.malurus.postservice.mapper.ViewMapper;
import com.malurus.postservice.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static com.malurus.postservice.constant.CacheName.POSTS_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class ViewService {

    private final ViewRepository viewRepository;
    private final ViewMapper viewMapper;
    private final MessageSourceService messageSourceService;
    private final CacheManager cacheManager;


    public Post createViewEntity(Post parentPost, String loggedInUser) {
        if (viewRepository.findByUserIdAndParentPostId(loggedInUser, parentPost.getId()).isEmpty()) {
            Optional.of(parentPost)
                    .map(post -> viewMapper.toEntity(post, loggedInUser))
                    .map(viewRepository::saveAndFlush)
                    .orElseThrow(() -> new CreateEntityException(
                            messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                    ));
        }
        Objects.requireNonNull(cacheManager.getCache(POSTS_CACHE_NAME)).evictIfPresent(parentPost.getId());
        return parentPost;
    }
}
