package com.malurus.userservice.mapper;

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

    User toEntity(CreateUserRequest createProfileRequest);

    @Mapping(target = "userId", source = "id")
    UserResponse toResponse(User profile);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "email", ignore = true),
            @Mapping(target = "joinDate", ignore = true)
    })
    User updateUserFromUpdateUserRequest(UpdateUserRequest updateProfileRequest, @MappingTarget User profile);
}
