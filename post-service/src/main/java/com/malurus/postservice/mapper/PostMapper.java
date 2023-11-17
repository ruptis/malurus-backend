package com.malurus.postservice.mapper;

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
    @Mapping(target = "userId", expression = "java(loggedInUser)")
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
            String loggedInUser
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", ignore = true)
    @Mapping(target = "userId", expression = "java(loggedInUser)")
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
            String loggedInUser
    );

    @Mapping(target = "userId", expression = "java(post.getUserId())")
    @Mapping(target = "quoteTo", expression = "java(this.toResponse(post.getQuoteTo(), loggedInUser, postUtil))")
    @Mapping(target = "replyTo", expression = "java(this.toResponse(post.getReplyTo(), loggedInUser, postUtil))")
    @Mapping(target = "repostTo", expression = "java(this.toResponse(post.getRepostTo(), loggedInUser, postUtil))")
    @Mapping(target = "likes", expression = "java(postUtil.countLikesForPost(post.getId()))")
    @Mapping(target = "replies", expression = "java(postUtil.countRepliesForPost(post.getId()))")
    @Mapping(target = "views", expression = "java(postUtil.countViewsForPost(post.getId()))")
    @Mapping(target = "reposts", expression = "java(postUtil.countRepostsForPost(post.getId()))")
    @Mapping(target = "isRePosted", expression = "java(postUtil.isPostRepostedByLoggedInUser(post.getId(), loggedInUser))")
    @Mapping(target = "isLiked", expression = "java(postUtil.isPostLikedByLoggedInUser(post.getId(), loggedInUser))")
    @Mapping(target = "isBelongs", expression = "java(postUtil.isPostBelongsToLoggedInUser(post.getId(), loggedInUser))")
    PostResponse toResponse(
            Post post,
            String loggedInUser,
            @Context PostUtil postUtil
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "reposts", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "replyTo", ignore = true)
    @Mapping(target = "repostTo", ignore = true)
    @Mapping(target = "quoteTo", ignore = true)
    Post updatePost(PostUpdateRequest request, @MappingTarget Post post);
}
