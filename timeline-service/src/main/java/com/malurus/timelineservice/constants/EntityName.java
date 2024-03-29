package com.malurus.timelineservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum EntityName {
    POSTS("posts"),
    REPOSTS("reposts"),
    REPLIES("replies");
    private final String name;
}
