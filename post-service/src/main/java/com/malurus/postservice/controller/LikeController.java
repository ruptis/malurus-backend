package com.malurus.postservice.controller;

import com.malurus.postservice.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, @RequestHeader String loggedInUser) {
        likeService.likePost(postId, loggedInUser);
        return ResponseEntity.status(CREATED).build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, @RequestHeader String loggedInUser) {
        likeService.unlikePost(postId, loggedInUser);
        return ResponseEntity.ok().build();
    }
}
