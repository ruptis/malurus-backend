package com.malurus.fanoutservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("${services.social-graph.name}")
public interface SocialGraphServiceClient {

    @GetMapping("/api/v1/social-graph/{userId}/followers")
    List<String> getFollowers(@PathVariable String userId);
}
