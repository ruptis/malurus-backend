package com.malurus.socialgraphservice.controller;

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
        return ResponseEntity.ok(socialGraphService.isFollowed(followeeId, loggedInUser));
    }

    @PostMapping("/{followeeId}")
    public ResponseEntity<Boolean> follow(@PathVariable String followeeId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(socialGraphService.follow(followeeId, loggedInUser));
    }

    @DeleteMapping("/{followeeId}")
    public ResponseEntity<Boolean> unfollow(@PathVariable String followeeId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(socialGraphService.unfollow(followeeId, loggedInUser));
    }

    @GetMapping("/{profileId}/followers")
    public ResponseEntity<List<String>> getFollowers(@PathVariable String profileId) {
        return ResponseEntity.ok(socialGraphService.getFollowers(profileId));
    }

    @GetMapping("/{profileId}/followees")
    public ResponseEntity<List<String>> getFollowees(@PathVariable String profileId) {
        return ResponseEntity.ok(socialGraphService.getFollowees(profileId));
    }

    @GetMapping("/{profileId}/followees-celebrities")
    public ResponseEntity<List<String>> getFolloweesCelebrities(@PathVariable String profileId) {
        return ResponseEntity.ok(socialGraphService.getFolloweesCelebrities(profileId));
    }
}
