package com.malurus.authenticationservice.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum JsonConstants {
    EXISTENT_ACCOUNT_JSON(AuthConstants.AUTH_REQ_PATTERN.getConstant().formatted(AuthConstants.EXISTENT_ACCOUNT_EMAIL.getConstant())),
    NEW_ACCOUNT_JSON(AuthConstants.AUTH_REQ_PATTERN.getConstant().formatted(AuthConstants.NEW_ACCOUNT_EMAIL.getConstant()));

    private final String constant;
}
