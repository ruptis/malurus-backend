package com.malurus.authenticationservice.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum AuthConstants {
    AUTH_REQ_PATTERN(
            """
                    {
                        "email": "%s",
                        "password": "test_password",
                        "username": "test_username"
                    }
                    """
    ),

    EXISTENT_ACCOUNT_EMAIL("test@gmail.com"),
    NEW_ACCOUNT_EMAIL("new_account@gmail.com");

    private final String constant;
}
