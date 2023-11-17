package com.malurus.postservice.controller;

import com.malurus.postservice.dto.request.PostCreateRequest;
import com.malurus.postservice.dto.request.PostUpdateRequest;
import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.service.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/reply/{replyToId}")
    public ResponseEntity<PostResponse> reply(
            @RequestPart(required = false) MultipartFile[] files,
            @Valid @RequestPart PostCreateRequest request,
            @PathVariable Long replyToId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(replyService.reply(request, replyToId, loggedInUser));
    }

    @GetMapping("/replies/user/{profileId}")
    public ResponseEntity<List<PostResponse>> getAllRepliesForUser(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(replyService.getAllRepliesForUser(profileId, PageRequest.of(page, size)));
    }

    @GetMapping("/replies/{replyToId}")
    public ResponseEntity<List<PostResponse>> getAllRepliesForPost(@PathVariable Long replyToId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(replyService.getAllRepliesForPost(replyToId, loggedInUser));
    }

    @GetMapping("/reply/{replyId}")
    public ResponseEntity<PostResponse> getReply(@PathVariable Long replyId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(replyService.getReplyById(replyId, loggedInUser));
    }

    @PatchMapping("/reply/{replyId}")
    public ResponseEntity<PostResponse> updateReply(
            @Valid @RequestPart PostUpdateRequest request,
            @RequestPart(required = false) MultipartFile[] files,
            @PathVariable Long replyId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(replyService.updateReply(replyId, request, loggedInUser, files));
    }

    @DeleteMapping("/reply/{replyId}")
    public ResponseEntity<Boolean> deleteReply(@PathVariable Long replyId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(replyService.deleteReply(replyId, loggedInUser));
    }
}
