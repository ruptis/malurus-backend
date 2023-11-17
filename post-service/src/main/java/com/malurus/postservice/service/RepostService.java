package com.malurus.postservice.service;

import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.mapper.PostMapper;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.util.PostUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.malurus.postservice.constant.CacheName.POSTS_CACHE_NAME;
import static com.malurus.postservice.constant.CacheName.REPOSTS_CACHE_NAME;
import static com.malurus.postservice.constant.Operation.ADD;
import static com.malurus.postservice.constant.Operation.DELETE;

@Service
@RequiredArgsConstructor
public class RepostService {

    private final PostMapper postMapper;
    private final PostUtil postUtil;
    private final PostRepository postRepository;
    private final MessageSourceService messageSourceService;
    private final CacheManager cacheManager;
    private final PostService postService;

    public boolean repost(Long repostToId, String loggedInUser) {
        postRepository.findById(repostToId)
                .map(post -> postMapper.toEntity(post, loggedInUser))
                .map(postRepository::saveAndFlush)
                .map(repost -> {
                    postUtil.sendMessageWithRepost(repost, ADD);
                    postUtil.evictEntityFromCache(repostToId, POSTS_CACHE_NAME);
                    return postMapper.toResponse(repost, loggedInUser, postUtil);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", repostToId)
                ));
        return true;
    }

    public boolean undoRepost(Long repostToId, String loggedInUser) {
        postRepository.findByRepostToIdAndUserId(repostToId, loggedInUser)
                .filter(repost -> postUtil.isEntityOwnedByLoggedInUser(repost, loggedInUser))
                .ifPresentOrElse(repost -> {
                    postUtil.evictEntityFromCache(repost.getId(), REPOSTS_CACHE_NAME);
                    postUtil.evictEntityFromCache(repostToId, POSTS_CACHE_NAME);
                    postUtil.sendMessageWithRepost(repost, DELETE);
                    postRepository.delete(repost);
                }, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", repostToId)
                    );
                });
        return true;
    }

    public PostResponse getRepostById(Long repostId, String loggedInUser) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(REPOSTS_CACHE_NAME));
        PostResponse repostResponse = cache.get(repostId, PostResponse.class);
        if (repostResponse != null) {
            return updateRepostResponse(repostResponse);
        }
        return postRepository.findByIdAndRepostToIsNotNull(repostId)
                .map(repost -> {
                    PostResponse response = postMapper.toResponse(repost, loggedInUser, postUtil);
                    cache.put(repostId, response);
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", repostId)
                ));
    }

    public List<PostResponse> getAllRepostsForUser(String userId, PageRequest page) {
        return postRepository.findAllByUserIdAndRepostToIsNotNullOrderByCreationDateDesc(userId, page)
                .stream()
                .map(repost -> postMapper.toResponse(repost, userId, postUtil))
                .collect(Collectors.toList());
    }

    private PostResponse updateRepostResponse(PostResponse repostResponse) {
        repostResponse.setRepostTo(postService.getPostById(
                repostResponse.getRepostTo().getId(),
                repostResponse.getUserId()
        ));
        return repostResponse;
    }
}
