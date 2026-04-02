package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic waitingListTopic() {
        return TopicBuilder.name("waiting-list-events")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic doctorTopic() {
        return TopicBuilder.name("doctor-events")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
