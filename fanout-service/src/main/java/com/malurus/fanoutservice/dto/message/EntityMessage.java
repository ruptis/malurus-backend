package com.malurus.fanoutservice.dto.message;

import lombok.Builder;

@Builder
public record EntityMessage (
        Long entityId,
        String profileId,
        String entityName,
        String operation
) {
}
