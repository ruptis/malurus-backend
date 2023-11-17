package com.malurus.postservice.mapper;

import com.malurus.postservice.client.ProfileServiceClient;
import com.malurus.postservice.dto.request.PostCreateRequest;
import com.malurus.postservice.dto.request.PostUpdateRequest;
import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.entity.Post;
import com.malurus.postservice.util.PostUtil;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", expression = "java(request.text())")
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "mediaUrls", ignore = true)
    @Mapping(target = "likes", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "reposts", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "replies", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "views", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "repostTo", ignore = true)
    @Mapping(target = "quoteTo", expression = "java(quoteTo)")
    @Mapping(target = "replyTo", expression = "java(replyTo)")
    Post toEntity(
            PostCreateRequest request,
            Post quoteTo,
            Post replyTo,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", ignore = true)
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "mediaUrls", ignore = true)
    @Mapping(target = "likes", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "reposts", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "replies", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "views", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "repostTo", expression = "java(repostTo)")
    @Mapping(target = "quoteTo", ignore = true)
    @Mapping(target = "replyTo", ignore = true)
    Post toEntity(
            Post repostTo,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );

    @Mapping(target = "profile", expression = "java(profileServiceClient.getProfileById(Post.getProfileId()))")
    @Mapping(target = "quoteTo", expression = "java(this.toResponse(Post.getQuoteTo(), loggedInUser, PostUtil, profileServiceClient))")
    @Mapping(target = "replyTo", expression = "java(this.toResponse(Post.getReplyTo(), loggedInUser, PostUtil, profileServiceClient))")
    @Mapping(target = "repostTo", expression = "java(this.toResponse(Post.getRePostTo(), loggedInUser, PostUtil, profileServiceClient))")
    @Mapping(target = "likes", expression = "java(PostUtil.countLikesForPost(Post.getId()))")
    @Mapping(target = "replies", expression = "java(PostUtil.countRepliesForPost(Post.getId()))")
    @Mapping(target = "views", expression = "java(PostUtil.countViewsForPost(Post.getId()))")
    @Mapping(target = "reposts", expression = "java(PostUtil.countRePostsForPost(Post.getId()))")
    @Mapping(target = "isRePosted", expression = "java(PostUtil.isPostRePostedByLoggedInUser(Post.getId(), loggedInUser, profileServiceClient))")
    @Mapping(target = "isLiked", expression = "java(PostUtil.isPostLikedByLoggedInUser(Post.getId(), loggedInUser, profileServiceClient))")
    @Mapping(target = "isBelongs", expression = "java(profileServiceClient.getProfileById(Post.getProfileId()).getEmail().equals(loggedInUser))")
    PostResponse toResponse(
            Post post,
            @Context String loggedInUser,
            @Context PostUtil postUtil,
            @Context ProfileServiceClient profileServiceClient
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "mediaUrls", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "reposts", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "replyTo", ignore = true)
    @Mapping(target = "repostTo", ignore = true)
    @Mapping(target = "quoteTo", ignore = true)
    Post updatePost(PostUpdateRequest request, @MappingTarget Post post);
}
