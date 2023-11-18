package com.malurus.socialgraphservice.mapper;

import com.malurus.socialgraphservice.dto.response.UserResponse;
import com.malurus.socialgraphservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "isCelebrity", expression = "java(user.isCelebrity())")
    UserResponse toResponse(User user);
}
