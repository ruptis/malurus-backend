package com.malurus.socialgraphservice.controller;

import com.malurus.socialgraphservice.dto.response.UserResponse;
import com.malurus.socialgraphservice.service.SocialGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/social-graph")
public class SocialGraphController {
    private final SocialGraphService socialGraphService;

    @GetMapping("/{followeeId}")
    public ResponseEntity<Boolean> isFollowed(@PathVariable String followeeId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(socialGraphService.isFollowed(loggedInUser, followeeId));
    }

    @GetMapping("/{userId}/celebrity")
    public ResponseEntity<Boolean> isCelebrity(@PathVariable String userId) {
        return ResponseEntity.ok(socialGraphService.isCelebrity(userId));
    }

    @PostMapping("/{followeeId}")
    public ResponseEntity<Boolean> follow(@PathVariable String followeeId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(socialGraphService.follow(loggedInUser, followeeId));
    }

    @DeleteMapping("/{followeeId}")
    public ResponseEntity<Boolean> unfollow(@PathVariable String followeeId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(socialGraphService.unfollow(loggedInUser, followeeId));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable String userId) {
        return ResponseEntity.ok(socialGraphService.getFollowers(userId));
    }

    @GetMapping("/{userId}/followees")
    public ResponseEntity<List<UserResponse>> getFollowees(@PathVariable String userId) {
        return ResponseEntity.ok(socialGraphService.getFollowees(userId));
    }

    @GetMapping("/{userId}/followees-celebrities")
    public ResponseEntity<List<UserResponse>> getFolloweesCelebrities(@PathVariable String userId) {
        return ResponseEntity.ok(socialGraphService.getFolloweesCelebrities(userId));
    }
}
