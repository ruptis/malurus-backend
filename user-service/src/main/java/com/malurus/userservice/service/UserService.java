package com.malurus.userservice.service;

import com.malurus.userservice.dto.request.CreateUserRequest;
import com.malurus.userservice.dto.request.UpdateUserRequest;
import com.malurus.userservice.dto.response.UserResponse;
import com.malurus.userservice.entity.User;
import com.malurus.userservice.exception.EntityNotFoundException;
import com.malurus.userservice.mapper.UserMapper;
import com.malurus.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.malurus.userservice.constant.CacheName.USERS_CACHE;
import static com.malurus.userservice.constant.TopicName.USER_CREATED;
import static com.malurus.userservice.constant.TopicName.USER_UPDATED;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaProducerService kafkaProducerService;
    private final MessageSourceService messageSourceService;

    public String createUser(CreateUserRequest createUserRequest) {
        return Optional.of(createUserRequest)
                .map(userMapper::toEntity)
                .map(userRepository::save)
                .map(user -> {
                    kafkaProducerService.send(user, USER_CREATED);
                    return user.getId();
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.unsuccessful_creation")
                ));
    }

    @Cacheable(cacheNames = USERS_CACHE, key = "#p0")
    public UserResponse getUser(String id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    @CachePut(cacheNames = USERS_CACHE, key = "#p0")
    public UserResponse updateUser(String loggedInUser, UpdateUserRequest updateUserRequest) {
        return userRepository.findById(loggedInUser)
                .map(user -> userMapper.updateUserFromUpdateUserRequest(updateUserRequest, user))
                .map(userRepository::save)
                .map(user -> {
                    kafkaProducerService.send(user, USER_UPDATED);
                    return userMapper.toResponse(user);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", loggedInUser)
                ));
    }

    public String getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", email)
                ));
    }

    public Page<UserResponse> findAllByUsername(String username, Pageable pageable) {
        return userRepository.findByUsernameContaining(username, pageable)
                .map(userMapper::toResponse);
    }

    public UserResponse getAuthUser(String loggedInUser) {
        return userRepository.findById(loggedInUser)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", loggedInUser)
                ));
    }
}
