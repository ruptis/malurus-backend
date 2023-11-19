package com.malurus.postservice.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public enum UrlConstants {

    POST_URL("/api/v1/post"),
    POSTS_URL("/api/v1/posts"),
    POST_URL_WITH_ID("/api/v1/post/%d"),
    POSTS_URL_WITH_ID("/api/v1/posts/%d"),
    LIKE_URL_WITH_ID("/api/v1/like/%d"),
    REPOST_URL("/api/v1/repost"),
    REPOST_URL_WITH_ID("/api/v1/repost/%d"),
    REPLY_URL_WITH_ID("/api/v1/reply/%d"),
    REPLIES_URL_WITH_ID("/api/v1/replies/%d");

    private final String constant;
}
