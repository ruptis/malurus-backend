package com.malurus.postservice.unit;

import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.entity.Post;
import com.malurus.postservice.mapper.PostMapper;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.service.MessageSourceService;
import com.malurus.postservice.service.PostService;
import com.malurus.postservice.service.RepostService;
import com.malurus.postservice.util.PostUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepostServiceTest {

    private static final String LOGGED_IN_USER = "user1";
    private static final Long REPOST_TO_ID = 2L;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostUtil postUtil;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private PostService postService;

    @InjectMocks
    private RepostService repostService;

    @Test
    void repost_ValidRequest_CreatesRepostAndReturnsTrue() {
        // Mocking
        Post post = new Post();
        Post repost = new Post();
        when(postRepository.findById(REPOST_TO_ID)).thenReturn(Optional.of(post));
        when(postMapper.toEntity(post, LOGGED_IN_USER)).thenReturn(repost);
        when(postRepository.saveAndFlush(repost)).thenReturn(repost);
        when(postMapper.toResponse(repost, LOGGED_IN_USER, postUtil)).thenReturn(new PostResponse());

        // Call repost
        boolean result = repostService.repost(REPOST_TO_ID, LOGGED_IN_USER);

        // Verify that the repost was created and the result is true
        verify(postRepository).saveAndFlush(repost);
        assertTrue(result);
    }

    // Similar tests can be written for undoRepost, getRepostById, getAllRepostsForUser, etc.

    @Test
    void undoRepost_ValidRequest_DeletesRepostAndReturnsTrue() {
        // Mocking
        Post repost = new Post();
        when(postRepository.findByRepostToIdAndUserId(REPOST_TO_ID, LOGGED_IN_USER)).thenReturn(Optional.of(repost));
        when(postUtil.isEntityOwnedByLoggedInUser(repost, LOGGED_IN_USER)).thenReturn(true);

        // Call undoRepost
        boolean result = repostService.undoRepost(REPOST_TO_ID, LOGGED_IN_USER);

        // Verify that the repost was deleted and the result is true
        verify(postRepository).delete(repost);
        assertTrue(result);
    }

    @Test
    void undoRepost_NonexistentRepost_ThrowsException() {
        // Mocking
        when(postRepository.findByRepostToIdAndUserId(REPOST_TO_ID, LOGGED_IN_USER)).thenReturn(Optional.empty());
        when(messageSourceService.generateMessage(anyString(), any())).thenReturn("Entity not found");

        // Call undoRepost and verify that it throws an EntityNotFoundException
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> repostService.undoRepost(REPOST_TO_ID, LOGGED_IN_USER));

        // Verify that the exception message is as expected
        assertEquals("Entity not found", exception.getMessage());
    }
}
