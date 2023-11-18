package com.malurus.socialgraphservice.dto.message;


public record UserMessage(
        String id,
        String email,
        String username,
        String bio,
        String location,
        String website
) {
}