package com.malurus.userservice.dto.message;


public record UserMessage(
        String id,
        String email,
        String username,
        String bio,
        String location,
        String website
) {
}