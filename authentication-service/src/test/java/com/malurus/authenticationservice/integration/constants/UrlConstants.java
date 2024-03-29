package com.malurus.authenticationservice.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum UrlConstants {
    REGISTER_URL("/api/v1/auth/register"),
    AUTHENTICATE_URL("/api/v1/auth/authenticate"),
    LOGOUT_URL("/api/v1/auth/logout");

    private final String constant;
}
