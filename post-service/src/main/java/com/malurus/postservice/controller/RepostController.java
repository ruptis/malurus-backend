package com.malurus.postservice.controller;

import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.service.RepostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RepostController {

    private final RepostService repostService;

    @PostMapping("/repost/{postId}")
    public ResponseEntity<Boolean> repost(@PathVariable Long postId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(repostService.repost(postId, loggedInUser));
    }

    @DeleteMapping("/repost/{postId}")
    public ResponseEntity<Boolean> undoRepost(@PathVariable Long postId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(repostService.undoRepost(postId, loggedInUser));
    }

    @GetMapping("/repost/{repostId}")
    public ResponseEntity<PostResponse> getRepost(@PathVariable Long repostId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(repostService.getRepostById(repostId, loggedInUser));
    }

    @GetMapping("/reposts/user/{profileId}")
    public ResponseEntity<List<PostResponse>> getAllRepostsForUser(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(repostService.getAllRepostsForUser(profileId, PageRequest.of(page, size)));
    }
}
