package com.malurus.timelineservice.client;

import com.malurus.timelineservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient("${services.profile.name}")
public interface UserServiceClient {

    @GetMapping("/api/v1/follows/{profileId}/followees")
    List<UserResponse> getFollowees(@PathVariable String profileId);

    @GetMapping("/api/v1/follows/{profileId}/followees-celebrities")
    List<UserResponse> getFolloweesCelebrities(@PathVariable String profileId);

    @GetMapping("/api/v1/profiles/id/{email}")
    String getProfileIdByLoggedInUser(@PathVariable String email);

    @GetMapping("/api/v1/profiles/{id}")
    UserResponse getProfileById(@PathVariable String id);

    @GetMapping("/api/v1/profiles/me")
    UserResponse getAuthProfile(@RequestHeader String loggedInUser);
}
