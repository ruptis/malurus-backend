package com.malurus.userservice.service;

import com.google.gson.Gson;
import com.malurus.userservice.entity.User;
import com.malurus.userservice.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson;
    private final UserMapper userMapper;

    public void send(User user, String topicName) {
        String userJson = gson.toJson(userMapper.toMessage(user));
        kafkaTemplate.send(topicName, userJson);
        log.info("User {} sent to topic {}", userJson, topicName);
    }
}
