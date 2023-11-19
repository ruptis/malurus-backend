package com.malurus.postservice.integration.tests;

import com.google.gson.Gson;
import com.malurus.postservice.dto.message.EntityMessage;
import com.malurus.postservice.entity.Post;
import com.malurus.postservice.integration.IntegrationTestBase;
import com.malurus.postservice.util.PostUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.malurus.postservice.constant.EntityName.POSTS;
import static com.malurus.postservice.constant.Operation.ADD;
import static com.malurus.postservice.constant.TopicName.USER_TIMELINE_TOPIC;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.utility.Base58.randomString;

@RequiredArgsConstructor
class KafkaTest extends IntegrationTestBase {

    private final PostUtil postUtil;
    private final Gson gson;
    private final KafkaTestConsumer kafkaConsumer;

    @Test
    @SneakyThrows
    void kafkaSendMessageTest() {
        String userId = randomString(15);
        Post post = buildDefaultPost(RandomUtils.nextLong(), userId);
        postUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, post, POSTS, ADD);

        boolean messageReceived = kafkaConsumer.getLatch().await(5, TimeUnit.SECONDS);
        EntityMessage receivedEntityMessage = gson.fromJson(kafkaConsumer.getPayload(), EntityMessage.class);

        assertTrue(messageReceived);
        assertNotNull(receivedEntityMessage);
        assertEquals(post.getId(), receivedEntityMessage.entityId());
        assertEquals(userId, receivedEntityMessage.userId());
    }

    private Post buildDefaultPost(Long id, String userId) {
        return Post.builder()
                .id(id)
                .userId(userId)
                .text(randomString(10))
                .creationDate(LocalDateTime.now())
                .build();
    }
}
