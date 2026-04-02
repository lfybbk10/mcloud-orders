package ru.mentee.power.orders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.topic.orders-events}")
    private String ordersEventsTopic;

    @Bean
    public NewTopic ordersEventsTopic() {
        return TopicBuilder.name(ordersEventsTopic)
                .partitions(10)
                .replicas(1)
                .build();
    }
}