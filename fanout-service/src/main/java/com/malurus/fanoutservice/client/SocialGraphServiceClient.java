package com.malurus.fanoutservice.client;

import com.malurus.fanoutservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "${services.social-graph.name}" , url = "${services.social-graph.url}")
public interface SocialGraphServiceClient {

    @GetMapping("/api/v1/social-graph/{userId}/followers")
    List<UserResponse> getFollowers(@PathVariable String userId);

    @GetMapping("/api/v1/social-graph/{userId}/celebrity")
    Boolean isCelebrity(@PathVariable String userId);
}
