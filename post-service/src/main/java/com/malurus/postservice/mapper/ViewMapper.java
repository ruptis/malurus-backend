package com.malurus.postservice.mapper;

import com.malurus.postservice.entity.Post;
import com.malurus.postservice.entity.View;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ViewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", expression = "java(loggedInUser)")
    @Mapping(target = "parentPost", expression = "java(parentPost)")
    View toEntity(
            Post parentPost,
            String loggedInUser
    );
}
