package com.malurus.fanoutservice.dto.message;

import lombok.Builder;

@Builder
public record EntityMessage (
        Long entityId,
        String userId,
        String entityName,
        String operation
) {
}
