package com.malurus.postservice.unit;

import com.malurus.postservice.dto.request.PostCreateRequest;
import com.malurus.postservice.dto.request.PostUpdateRequest;
import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.entity.Post;
import com.malurus.postservice.exception.CreateEntityException;
import com.malurus.postservice.mapper.PostMapper;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.service.MessageSourceService;
import com.malurus.postservice.service.PostService;
import com.malurus.postservice.service.ViewService;
import com.malurus.postservice.util.PostUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private static final String LOGGED_IN_USER = "user1";
    private static final Long POST_ID = 1L;

    @Mock
    private PostMapper postMapper;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostUtil postUtil;

    @Mock
    private ViewService viewService;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private PostService postService;

    @Test
    void createPost_ValidRequest_CreatesPostAndReturnsResponse() {
        // Mocking
        PostCreateRequest request = new PostCreateRequest("Post content");
        Post post = new Post();
        PostResponse postResponse = new PostResponse();
        when(postMapper.toEntity(request, null, null, LOGGED_IN_USER)).thenReturn(post);
        when(postRepository.saveAndFlush(post)).thenReturn(post);
        when(postMapper.toResponse(post, LOGGED_IN_USER, postUtil)).thenReturn(postResponse);

        // Call createPost
        PostResponse result = postService.createPost(request, LOGGED_IN_USER);

        // Verify that the result is the expected PostResponse
        assertEquals(postResponse, result);
    }

    @Test
    void createPost_InvalidRequest_ThrowsException() {
        // Mocking
        PostCreateRequest request = new PostCreateRequest("Post content");
        when(postMapper.toEntity(request, null, null, LOGGED_IN_USER)).thenReturn(null);
        when(messageSourceService.generateMessage(anyString())).thenReturn("Error message");

        // Call createPost and verify that it throws a CreateEntityException
        CreateEntityException exception = assertThrows(CreateEntityException.class,
                () -> postService.createPost(request, LOGGED_IN_USER));

        // Verify that the exception message is as expected
        assertEquals("Error message", exception.getMessage());
    }

    // Similar tests can be written for other methods like createQuotePost, getPostById, getAllPostsForUser, etc.

    @Test
    void updatePost_ValidRequest_CallsRepositoriesAndMapper() {
        // Mocking
        PostUpdateRequest request = new PostUpdateRequest("Updated post content");
        Post post = new Post();
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(postUtil.isEntityOwnedByLoggedInUser(post, LOGGED_IN_USER)).thenReturn(true);
        when(postMapper.updatePost(request, post)).thenReturn(post);
        when(postRepository.saveAndFlush(post)).thenReturn(post);
        when(postMapper.toResponse(post, LOGGED_IN_USER, postUtil)).thenReturn(new PostResponse());

        // Call updatePost
        postService.updatePost(POST_ID, request, LOGGED_IN_USER);

        // Verify that repositories and mapper were called with the expected arguments
        verify(postRepository).findById(POST_ID);
        verify(postUtil).isEntityOwnedByLoggedInUser(post, LOGGED_IN_USER);
        verify(postRepository).saveAndFlush(post);
        verify(postMapper).toResponse(post, LOGGED_IN_USER, postUtil);
    }

    // Similar tests can be written for deletePost and other methods

    @Test
    void updatePost_NonexistentPost_ThrowsException() {
        // Mocking
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());
        when(messageSourceService.generateMessage(anyString(), any())).thenReturn("Entity not found");
        // Call updatePost and verify that it throws an EntityNotFoundException
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> postService.updatePost(POST_ID, new PostUpdateRequest("Updated post content"), LOGGED_IN_USER));

        // Verify that the exception message is as expected
        assertEquals("Entity not found", exception.getMessage());
    }

    @Test
    void updatePost_PostNotOwnedByUser_ThrowsException() {
        // Mocking
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(new Post()));
        when(postUtil.isEntityOwnedByLoggedInUser(any(), eq(LOGGED_IN_USER))).thenReturn(false);
        when(messageSourceService.generateMessage(anyString(), any())).thenReturn("Not owned by user");

        // Call updatePost and verify that it throws an EntityNotFoundException
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> postService.updatePost(POST_ID, new PostUpdateRequest("Updated post content"), LOGGED_IN_USER));

        // Verify that the exception message is as expected
        assertEquals("Not owned by user", exception.getMessage());
    }
}
