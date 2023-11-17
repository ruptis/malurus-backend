package com.malurus.postservice.service;

import com.malurus.postservice.client.ProfileServiceClient;
import com.malurus.postservice.entity.Post;
import com.malurus.postservice.exception.CreateEntityException;
import com.malurus.postservice.mapper.ViewMapper;
import com.malurus.postservice.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ViewService {

    private final ViewRepository viewRepository;
    private final ViewMapper viewMapper;
    private final MessageSourceService messageSourceService;


    public Post createViewEntity(Post parentPost, String loggedInUser, ProfileServiceClient profileServiceClient) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        if (viewRepository.findByProfileIdAndParentPostId(profileId, parentPost.getId()).isEmpty()) {
            Optional.of(parentPost)
                    .map(post -> viewMapper.toEntity(post, loggedInUser, profileServiceClient))
                    .map(viewRepository::saveAndFlush)
                    .orElseThrow(() -> new CreateEntityException(
                            messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                    ));
        }
        return parentPost;
    }
}
