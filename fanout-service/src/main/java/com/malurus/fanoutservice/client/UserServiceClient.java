package com.malurus.fanoutservice.client;

import com.malurus.fanoutservice.dto.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("${services.user.name}")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/id/{email}")
    String getProfileIdByLoggedInUser(@PathVariable String email);

    @GetMapping("/api/v1/users/{id}")
    ProfileResponse getProfileById(@PathVariable String id);

    @GetMapping("/api/v1/follows/{profileId}/followers")
    List<ProfileResponse> getFollowers(@PathVariable String profileId);
}
