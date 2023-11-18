package com.malurus.userservice.mapper;

import com.malurus.userservice.dto.message.UserMessage;
import com.malurus.userservice.dto.request.CreateUserRequest;
import com.malurus.userservice.dto.request.UpdateUserRequest;
import com.malurus.userservice.dto.response.UserResponse;
import com.malurus.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(CreateUserRequest createUserRequest);

    @Mapping(target = "userId", source = "id")
    UserResponse toResponse(User user);

    UserMessage toMessage(User user);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "email", ignore = true),
            @Mapping(target = "joinDate", ignore = true)
    })
    User updateUserFromUpdateUserRequest(UpdateUserRequest updateUserRequest, @MappingTarget User profile);
}
