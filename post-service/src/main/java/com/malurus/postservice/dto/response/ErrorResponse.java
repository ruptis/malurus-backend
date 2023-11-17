package com.malurus.postservice.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        String message,
        Integer code,
        LocalDateTime timestamp
) {
}
