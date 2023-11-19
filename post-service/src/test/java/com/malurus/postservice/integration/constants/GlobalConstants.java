package com.malurus.postservice.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum GlobalConstants {

    EMAIL("dummy-email"),
    USERNAME("dummy-username"),
    ID("dummy-id"),

    DEFAULT_POST_TEXT("some text"),
    UPDATE_POST_TEXT("updated text"),
    DEFAULT_REPLY_TEXT("reply text"),
    UPDATE_REPLY_TEXT("updated reply text"),

    TEXT_EMPTY_MESSAGE("Text shouldn't be empty."),
    ERROR_DUPLICATE_ENTITY("could not execute statement");


    private final String constant;
}
