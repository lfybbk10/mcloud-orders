package ru.mentee.power.orders.adapters.kafka;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.adapters.metrics.ProducerMetricsRegistry;
import ru.mentee.power.orders.exception.OrderPublishException;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

@Component
@RequiredArgsConstructor
public class OrderEventProducer implements OrderEventPort {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final KafkaTopicResolver kafkaTopicResolver;

    private final ProducerMetricsRegistry producerMetricsRegistry;


    @Override
    public void publish(OrderPlacedEvent event) {
        var topicName = kafkaTopicResolver.resolveTopic(event.priority());
        var producerRecord = new ProducerRecord<>(topicName, event.region(), event);

        try {
            kafkaTemplate.send(producerRecord).join();
            producerMetricsRegistry.recordSuccess(topicName, event.region());
        } catch (Exception ex) {
            producerMetricsRegistry.recordFailure(topicName, event.region());
            throw new OrderPublishException("Не удалось отправить событие в Kafka", ex);
        }
    }
}