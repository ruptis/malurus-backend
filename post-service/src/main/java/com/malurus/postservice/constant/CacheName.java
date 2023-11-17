package com.malurus.postservice.constant;

import org.springframework.stereotype.Component;

@Component
public class CacheName {

    public static final String POSTS_CACHE_NAME = "Posts";
    public static final String REPOSTS_CACHE_NAME = "rePosts";
    public static final String REPLIES_CACHE_NAME = "replies";
    public static final String REPLIES_FOR_POST_CACHE_NAME = "repliesForPost";
}
