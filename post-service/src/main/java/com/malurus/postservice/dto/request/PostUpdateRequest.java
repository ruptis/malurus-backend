package com.malurus.postservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record PostUpdateRequest(
        @NotEmpty(message = "{text.not_empty}")
        @NotBlank(message = "{text.not_empty}")
        String text
) {
}
