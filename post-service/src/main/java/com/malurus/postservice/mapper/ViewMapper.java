package com.malurus.postservice.mapper;

import com.malurus.postservice.client.ProfileServiceClient;
import com.malurus.postservice.entity.Post;
import com.malurus.postservice.entity.View;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ViewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "parentPost", expression = "java(parentPost)")
    View toEntity(
            Post parentPost,
            @Context String loggedInUser,
            @Context ProfileServiceClient profileServiceClient
    );
}
