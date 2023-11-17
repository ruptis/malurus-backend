package com.malurus.postservice.service;

import com.malurus.postservice.dto.request.PostCreateRequest;
import com.malurus.postservice.dto.request.PostUpdateRequest;
import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.mapper.PostMapper;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.util.PostUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.malurus.postservice.constant.CacheName.REPLIES_CACHE_NAME;
import static com.malurus.postservice.constant.CacheName.REPLIES_FOR_POST_CACHE_NAME;
import static com.malurus.postservice.constant.Operation.ADD;
import static com.malurus.postservice.constant.Operation.DELETE;
import static com.malurus.postservice.util.PostUtil.EvictionStrategy.CACHE_ONLY;
import static com.malurus.postservice.util.PostUtil.EvictionStrategy.WITH_TIMELINE;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final PostRepository postRepository;
    private final PostUtil postUtil;
    private final PostMapper postMapper;
    private final MessageSourceService messageSourceService;
    private final ViewService viewService;
    private final PostService postService;
    private final CacheManager cacheManager;

    public PostResponse reply(PostCreateRequest request, Long replyToId, String loggedInUser) {
        return postRepository.findById(replyToId)
                .map(replyTo -> postMapper.toEntity(request, null, replyTo, loggedInUser))
                .map(postRepository::saveAndFlush)
                .map(reply -> {
                    postUtil.evictEntityFromCache(reply.getReplyTo().getId(), REPLIES_FOR_POST_CACHE_NAME);
                    postUtil.sendMessageWithReply(reply, ADD);
                    return postMapper.toResponse(reply, loggedInUser, postUtil);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", replyToId)
                ));
    }

    @CacheEvict(cacheNames = REPLIES_CACHE_NAME, key = "#p0")
    public boolean deleteReply(Long replyId, String loggedInUser) {
        postRepository.findById(replyId)
                .filter(reply -> postUtil.isEntityOwnedByLoggedInUser(reply, loggedInUser))
                .ifPresentOrElse(reply -> {
                    postUtil.evictEntityFromCache(reply.getReplyTo().getId(), REPLIES_FOR_POST_CACHE_NAME);
                    postUtil.evictAllEntityRelationsFromCache(reply, WITH_TIMELINE);
                    postUtil.sendMessageWithReply(reply, DELETE);
                    postRepository.delete(reply);
                }, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", replyId)
                    );
                });
        return true;
    }

    @CachePut(cacheNames = REPLIES_CACHE_NAME, key = "#p0")
    public PostResponse updateReply(Long replyId, PostUpdateRequest request, String loggedInUser) {
        return postRepository.findById(replyId)
                .filter(reply -> postUtil.isEntityOwnedByLoggedInUser(reply, loggedInUser))
                .map(reply -> postMapper.updatePost(request, reply))
                .map(postRepository::saveAndFlush)
                .map(reply -> {
                    postUtil.evictEntityFromCache(reply.getReplyTo().getId(), REPLIES_FOR_POST_CACHE_NAME);
                    postUtil.evictAllEntityRelationsFromCache(reply, CACHE_ONLY);
                    return postMapper.toResponse(reply, loggedInUser, postUtil);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", replyId)
                ));

    }

    public PostResponse getReplyById(Long replyId, String loggedInUser) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(REPLIES_CACHE_NAME));
        PostResponse replyResponse = cache.get(replyId, PostResponse.class);
        if (replyResponse != null) {
            return updateReplyResponse(replyResponse);
        }
        return postRepository.findByIdAndReplyToIsNotNull(replyId)
                .map(reply -> viewService.createViewEntity(reply, loggedInUser))
                .map(reply -> {
                    PostResponse response = postMapper.toResponse(reply, loggedInUser, postUtil);
                    cache.put(replyId, response);
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", replyId)
                ));
    }

    public List<PostResponse> getAllRepliesForUser(String userId, PageRequest page) {
        return postRepository.findAllByUserIdAndReplyToIsNotNullOrderByCreationDateDesc(userId, page)
                .stream()
                .map(reply -> postMapper.toResponse(reply, userId, postUtil))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<PostResponse> getAllRepliesForPost(Long replyToId, String loggedInUser) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(REPLIES_FOR_POST_CACHE_NAME));
        List<PostResponse> replyResponses = cache.get(replyToId, List.class);
        if (replyResponses != null) {
            return replyResponses.stream()
                    .map(this::updateReplyResponse)
                    .collect(Collectors.toList());
        }

        replyResponses = postRepository.findAllByReplyToIdOrderByCreationDateDesc(replyToId)
                .stream()
                .map(reply -> postMapper.toResponse(reply, loggedInUser, postUtil))
                .collect(Collectors.toList());
        cache.put(replyToId, replyResponses);
        return replyResponses;
    }

    private PostResponse updateReplyResponse(PostResponse replyResponse) {
        replyResponse.setReplyTo(postService.getPostById(
                replyResponse.getReplyTo().getId(),
                replyResponse.getUserId()
        ));
        return replyResponse;
    }
}
