package com.malurus.timelineservice.controller;

import com.malurus.timelineservice.dto.response.PostResponse;
import com.malurus.timelineservice.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/user")
    public ResponseEntity<List<PostResponse>> getUserTimelineForLoggedInUser(
            @RequestHeader String loggedInUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getUserTimeline(loggedInUser, PageRequest.of(page, size)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getUserTimelineForAnotherUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getUserTimeline(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/user-replies")
    public ResponseEntity<List<PostResponse>> getRepliesUserTimelineForLoggedInUser(
            @RequestHeader String loggedInUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getRepliesUserTimeline(loggedInUser, PageRequest.of(page, size)));
    }

    @GetMapping("/user-replies/{userId}")
    public ResponseEntity<List<PostResponse>> getRepliesUserTimelineForAnotherUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getRepliesUserTimeline(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/home")
    public ResponseEntity<List<PostResponse>> getHomeTimeline(
            @RequestHeader String loggedInUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getHomeTimeline(loggedInUser, PageRequest.of(page, size)));
    }
}
