package com.malurus.postservice.service;

import com.malurus.postservice.client.ProfileServiceClient;
import com.malurus.postservice.mapper.LikeMapper;
import com.malurus.postservice.repository.LikeRepository;
import com.malurus.postservice.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final ProfileServiceClient profileServiceClient;
    private final LikeMapper likeMapper;
    private final MessageSourceService messageSourceService;

    public void likePost(Long postId, String loggedInUser) {
        postRepository.findById(postId)
                .map(post -> likeMapper.toEntity(post, profileServiceClient, loggedInUser))
                .map(likeRepository::saveAndFlush)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", postId)
                ));
    }

    public void unlikePost(Long postId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        likeRepository.findByParentPostIdAndProfileId(postId, profileId)
                .ifPresentOrElse(likeRepository::delete, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", postId)
                    );
                });
    }
}
