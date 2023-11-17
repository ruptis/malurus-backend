package com.malurus.postservice.controller;

import com.malurus.postservice.dto.request.PostCreateRequest;
import com.malurus.postservice.dto.request.PostUpdateRequest;
import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

    private final PostService postService;

    @PostMapping("/post")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestPart PostCreateRequest request,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(postService.createPost(request, loggedInUser));
    }

    @PostMapping(value = "/post/{postId}")
    public ResponseEntity<PostResponse> createQuotePost(
            @Valid @RequestPart PostCreateRequest request,
            @PathVariable Long postId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(postService.createQuotePost(request, postId, loggedInUser));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(postService.getPostById(postId, loggedInUser));
    }

    @GetMapping("/posts/user/{userId}")
    public ResponseEntity<List<PostResponse>> getAllPostsForUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(postService.getAllPostsForUser(userId, PageRequest.of(page, size)));
    }

    @PatchMapping(value = "/post/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @Valid @RequestPart PostUpdateRequest request,
            @PathVariable Long postId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(postService.updatePost(postId, request, loggedInUser));
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Boolean> deletePost(
            @PathVariable Long postId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(postService.deletePost(postId, loggedInUser));
    }
}
