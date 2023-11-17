package com.malurus.postservice.service;

import com.malurus.postservice.dto.request.PostCreateRequest;
import com.malurus.postservice.dto.request.PostUpdateRequest;
import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.exception.CreateEntityException;
import com.malurus.postservice.mapper.PostMapper;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.util.PostUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.malurus.postservice.constant.CacheName.POSTS_CACHE_NAME;
import static com.malurus.postservice.constant.Operation.ADD;
import static com.malurus.postservice.constant.Operation.DELETE;
import static com.malurus.postservice.util.PostUtil.EvictionStrategy.CACHE_ONLY;
import static com.malurus.postservice.util.PostUtil.EvictionStrategy.WITH_TIMELINE;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final MessageSourceService messageSourceService;
    private final PostRepository postRepository;
    private final PostUtil postUtil;
    private final ViewService viewService;
    private final CacheManager cacheManager;

    public PostResponse createPost(PostCreateRequest request, String loggedInUser) {
        return Optional.of(request)
                .map(req -> postMapper.toEntity(req, null, null, loggedInUser))
                .map(postRepository::saveAndFlush)
                .map(post -> {
                    postUtil.sendMessageWithPost(post, ADD);
                    return postMapper.toResponse(post, loggedInUser, postUtil);
                })
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public PostResponse createQuotePost(PostCreateRequest request, Long postId, String loggedInUser) {
        return postRepository.findById(postId)
                .map(quoteToPost -> postMapper.toEntity(request, quoteToPost, null, loggedInUser))
                .map(postRepository::saveAndFlush)
                .map(post -> {
                    postUtil.sendMessageWithPost(post, ADD);
                    return postMapper.toResponse(post, loggedInUser, postUtil);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", postId)
                ));
    }

    public PostResponse getPostById(Long postId, String loggedInUser) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(POSTS_CACHE_NAME));
        PostResponse postResponse = cache.get(postId, PostResponse.class);
        if (postResponse != null) {
            return updatePostResponse(postResponse);
        }
        return postRepository.findById(postId)
                .map(post -> viewService.createViewEntity(post, loggedInUser))
                .map(post -> {
                    PostResponse response = postMapper.toResponse(post, loggedInUser, postUtil);
                    cache.put(postId, response);
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", postId)
                ));
    }

    public List<PostResponse> getAllPostsForUser(String userId, PageRequest page) {
        return postRepository.findAllByUserIdAndReplyToIsNullAndRepostToIsNullOrderByCreationDateDesc(userId, page)
                .stream()
                .map(post -> postMapper.toResponse(post, userId, postUtil))
                .toList();
    }

    @CachePut(cacheNames = POSTS_CACHE_NAME, key = "#p0")
    public PostResponse updatePost(Long postId, PostUpdateRequest request, String loggedInUser) {
        return postRepository.findById(postId)
                .filter(post -> postUtil.isEntityOwnedByLoggedInUser(post, loggedInUser))
                .map(post -> postMapper.updatePost(request, post))
                .map(postRepository::saveAndFlush)
                .map(post -> {
                    postUtil.evictAllEntityRelationsFromCache(post, CACHE_ONLY);
                    return postMapper.toResponse(post, loggedInUser, postUtil);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", postId)
                ));
    }

    @CacheEvict(cacheNames = POSTS_CACHE_NAME, key = "#p0")
    public Boolean deletePost(Long postId, String loggedInUser) {
        return postRepository.findById(postId)
                .filter(post -> postUtil.isEntityOwnedByLoggedInUser(post, loggedInUser))
                .map(post -> {
                    postUtil.sendMessageWithPost(post, DELETE);
                    postUtil.evictAllEntityRelationsFromCache(post, WITH_TIMELINE);
                    postRepository.delete(post);
                    return post;
                })
                .isPresent();
    }

    private PostResponse updatePostResponse(PostResponse postResponse) {
        PostResponse quoteTo = postResponse.getQuoteTo();
        if (quoteTo != null) {
            postResponse.setQuoteTo(getPostById(
                    quoteTo.getId(),
                    postResponse.getUserId()
            ));
        }
        return postResponse;
    }
}
