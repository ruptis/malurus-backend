package com.malurus.postservice.constant;

import org.springframework.stereotype.Component;

@Component
public class CacheName {

    public static final String POSTS_CACHE_NAME = "posts";
    public static final String REPOSTS_CACHE_NAME = "reposts";
    public static final String REPLIES_CACHE_NAME = "replies";
    public static final String REPLIES_FOR_POST_CACHE_NAME = "repliesForPost";
}
