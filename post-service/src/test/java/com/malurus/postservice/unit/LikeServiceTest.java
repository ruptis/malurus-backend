package com.malurus.postservice.unit;

import com.malurus.postservice.entity.Like;
import com.malurus.postservice.entity.Post;
import com.malurus.postservice.mapper.LikeMapper;
import com.malurus.postservice.repository.LikeRepository;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.service.LikeService;
import com.malurus.postservice.service.MessageSourceService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private MessageSourceService messageSourceService;

    @InjectMocks
    private LikeService likeService;

    @Test
    void likePost_ValidPostIdAndUser_CallsRepositoriesAndMapper() {
        Long postId = 1L;
        String loggedInUser = "user1";

        // Mocking
        Post post = new Post();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Like likeEntity = new Like();
        when(likeMapper.toEntity(post, loggedInUser)).thenReturn(likeEntity);
        when(likeRepository.saveAndFlush(likeEntity)).thenReturn(likeEntity);

        // Call likePost
        likeService.likePost(postId, loggedInUser);

        // Verify that repositories and mapper were called with the expected arguments
        verify(postRepository).findById(postId);
        verify(likeMapper).toEntity(post, loggedInUser);
        verify(likeRepository).saveAndFlush(likeEntity);
    }

    @Test
    void likePost_InvalidPostId_ThrowsException() {
        Long postId = 1L;
        String loggedInUser = "user1";

        // Mocking
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        when(messageSourceService.generateMessage(anyString(), any())).thenReturn("Entity not found");

        // Call likePost and verify that it throws an EntityNotFoundException
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> likeService.likePost(postId, loggedInUser));

        // Verify that the exception message is as expected
        assertEquals("Entity not found", exception.getMessage());
    }

    @Test
    void unlikePost_ExistingLike_CallsRepositoryDelete() {
        Long postId = 1L;
        String loggedInUser = "user1";

        // Mocking
        when(likeRepository.findByParentPostIdAndUserId(postId, loggedInUser)).thenReturn(Optional.of(mock(Like.class)));

        // Call unlikePost
        likeService.unlikePost(postId, loggedInUser);

        // Verify that likeRepository.delete was called
        verify(likeRepository).delete(any());
    }

    @Test
    void unlikePost_NonexistentLike_ThrowsException() {
        Long postId = 1L;
        String loggedInUser = "user1";

        // Mocking
        when(likeRepository.findByParentPostIdAndUserId(postId, loggedInUser)).thenReturn(Optional.empty());
        when(messageSourceService.generateMessage(anyString(), any())).thenReturn("Entity not found");

        // Call unlikePost and verify that it throws an EntityNotFoundException
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> likeService.unlikePost(postId, loggedInUser));

        // Verify that the exception message is as expected
        assertEquals("Entity not found", exception.getMessage());
    }
}
