package com.malurus.authenticationservice.client.request;

import java.time.LocalDate;

public record CreateUserRequest(
        String username,
        String email,
        LocalDate joinDate
) {

}
