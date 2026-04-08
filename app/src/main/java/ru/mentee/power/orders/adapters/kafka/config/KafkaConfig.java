package ru.mentee.power.orders.adapters.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, OrderPlacedEvent> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32_768);

        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, OrderPlacedEvent> orderPlacedConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "orders-processor-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<OrderPlacedEvent> jsonDeserializer =
                new JsonDeserializer<>(OrderPlacedEvent.class);

        jsonDeserializer.addTrustedPackages("ru.mentee.power.orders.adapters.kafka.message");
        jsonDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> orderListenerFactory(
            ConsumerFactory<String, OrderPlacedEvent> orderPlacedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderPlacedConsumerFactory);

        // чтобы работал Acknowledgment ack
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // если захочешь параллельную обработку
        factory.setConcurrency(3);

        return factory;
    }

    @Bean
    public KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate(
            ProducerFactory<String, OrderPlacedEvent> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic ordersPriorityHigh() {
        return TopicBuilder.name("orders.priority.high")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ordersPriorityNormal() {
        return TopicBuilder.name("orders.priority.normal")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ordersPriorityLow() {
        return TopicBuilder.name("orders.priority.low")
                .partitions(3)
                .replicas(1)
                .build();
    }

}