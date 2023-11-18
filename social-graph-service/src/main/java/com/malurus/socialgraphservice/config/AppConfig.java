package com.malurus.socialgraphservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor kafkaExecutor = new ThreadPoolTaskExecutor();
        kafkaExecutor.setCorePoolSize(3);
        kafkaExecutor.setMaxPoolSize(3);
        kafkaExecutor.setQueueCapacity(10);
        kafkaExecutor.setThreadNamePrefix("KafkaExecutor-");
        kafkaExecutor.initialize();
        return kafkaExecutor;
    }
}
