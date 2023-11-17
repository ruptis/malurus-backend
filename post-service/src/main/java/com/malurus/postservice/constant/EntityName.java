package com.malurus.postservice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EntityName {
    POSTS("Posts"),
    REPOSTS("rePosts"),
    REPLIES("replies");
    private final String name;
}
