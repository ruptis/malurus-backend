package com.malurus.timelineservice.client;

import com.malurus.timelineservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "${services.social-graph.name}" , url = "${services.social-graph.url}")
public interface SocialGraphServiceClient {

    @GetMapping("/api/v1/social-graph/{userId}/followees")
    List<UserResponse> getFollowees(@PathVariable String userId);

    @GetMapping("/api/v1/social-graph/{userId}/followees-celebrities")
    List<UserResponse> getFolloweesCelebrities(@PathVariable String userId);

    @GetMapping("/api/v1/social-graph/{userId}/celebrity")
    Boolean isCelebrity(@PathVariable String userId);
}
