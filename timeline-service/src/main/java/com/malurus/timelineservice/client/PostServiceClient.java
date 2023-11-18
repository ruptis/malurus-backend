package com.malurus.timelineservice.client;

import com.malurus.timelineservice.dto.response.PostResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("${services.post.name}")
public interface PostServiceClient {

    @GetMapping("/api/v1/posts/user/{userId}")
    List<PostResponse> getAllPostsForUser(
            @PathVariable String userId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/reposts/user/{userId}")
    List<PostResponse> getAllRepostsForUser(
            @PathVariable String userId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/replies/user/{userId}")
    List<PostResponse> getAllRepliesForUser(
            @PathVariable String userId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/post/{postId}")
    PostResponse getPost(@PathVariable Long postId, @RequestHeader String loggedInUser);

    @GetMapping("/api/v1/repost/{repostId}")
    PostResponse getRepost(@PathVariable Long repostId, @RequestHeader String loggedInUser);

    @GetMapping("/api/v1/reply/{replyId}")
    PostResponse getReply(@PathVariable Long replyId, @RequestHeader String loggedInUser);
}
