package com.malurus.postservice.integration;

import com.malurus.postservice.integration.annotation.IT;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;

import static com.malurus.postservice.constant.CacheName.*;
import static com.malurus.postservice.constant.TopicName.USER_TIMELINE_TOPIC;

@IT
@Import(IntegrationTestBase.KafkaTestConsumer.class)
@DirtiesContext
public class IntegrationTestBase {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine3.17"));
    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-rc-alpine3.18")).withExposedPorts(6379);
    @Container
    private static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.2"))
            .waitingFor(Wait.forListeningPort());
    @Container
    @SuppressWarnings({"resource", "unused", "deprecation"})
    private static final GenericContainer<?> configServerContainer = new FixedHostPortGenericContainer<>(
            "kazyshi/malurus-spring-cloud-config-server:0.5"
    )
            .withFixedExposedPort(8888, 8888)
            .waitingFor(Wait.forHttp("/post-service/test")
                    .forStatusCodeMatching(port -> port >= 200 && port < 400));

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    @SuppressWarnings("DataFlowIssue")
    public void setUp() {
        cacheManager.getCache(POSTS_CACHE_NAME).clear();
        cacheManager.getCache(REPOSTS_CACHE_NAME).clear();
        cacheManager.getCache(REPLIES_CACHE_NAME).clear();
        cacheManager.getCache(REPLIES_FOR_POST_CACHE_NAME).clear();
    }

    @DynamicPropertySource
    static void dataSourcesProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Data
    @TestComponent
    @Slf4j
    protected static class KafkaTestConsumer {

        private String payload;
        private CountDownLatch latch = new CountDownLatch(1);

        @KafkaListener(topics = USER_TIMELINE_TOPIC)
        public void receive(String msg) {
            log.info("received message {}", msg);
            payload = msg;
            latch.countDown();
        }
    }
}
