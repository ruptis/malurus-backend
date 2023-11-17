package com.malurus.postservice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EntityName {
    POSTS("posts"),
    REPOSTS("reposts"),
    REPLIES("replies");
    private final String name;
}
