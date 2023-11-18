package com.malurus.socialgraphservice.service;

import com.google.gson.Gson;
import com.malurus.socialgraphservice.dto.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.malurus.socialgraphservice.constant.TopicName.USER_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final SocialGraphService socialGraphService;
    private final Gson gson;

    @Async
    @KafkaListener(topics = USER_CREATED)
    public CompletableFuture<Void> consumeUserCreated(String userJson) {
        log.info("Consuming user created event: {}", userJson);
        String userId = gson.fromJson(userJson, UserMessage.class).id();
        socialGraphService.addUser(userId);
        return CompletableFuture.completedFuture(null);
    }
}
