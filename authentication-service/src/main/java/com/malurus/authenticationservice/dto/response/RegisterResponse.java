package com.malurus.authenticationservice.dto.response;

import lombok.Builder;

@Builder
public record RegisterResponse(
        String message
) {
}
