package com.malurus.postservice.mapper;

import com.malurus.postservice.entity.Like;
import com.malurus.postservice.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentPost", expression = "java(parentPost)")
    @Mapping(target = "userId", expression = "java(loggedInUser)")
    Like toEntity (
            Post parentPost,
            String loggedInUser
    );
}
