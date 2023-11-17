package com.malurus.timelineservice.client;

import com.malurus.timelineservice.dto.response.PostResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("${services.tweet.name}")
public interface PostServiceClient {

    @GetMapping("/api/v1/tweets/user/{profileId}")
    List<PostResponse> getAllTweetsForUser(
            @PathVariable String profileId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/retweets/user/{profileId}")
    List<PostResponse> getAllRetweetsForUser(
            @PathVariable String profileId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/replies/user/{profileId}")
    List<PostResponse> getAllRepliesForUser(
            @PathVariable String profileId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/tweet/{tweetId}")
    PostResponse getTweet(@PathVariable Long tweetId, @RequestHeader String loggedInUser);

    @GetMapping("/api/v1/retweet/{retweetId}")
    PostResponse getRetweet(@PathVariable Long retweetId, @RequestHeader String loggedInUser);

    @GetMapping("/api/v1/reply/{replyId}")
    PostResponse getReply(@PathVariable Long replyId, @RequestHeader String loggedInUser);
}
