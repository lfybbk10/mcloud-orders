package ru.mentee.power.orders.adapters.kafka;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.mentee.power.orders.adapters.metrics.ConsumerMetricsRegistry;
import ru.mentee.power.orders.adapters.persistence.OrderRepository;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 3,
        topics = {"orders.priority.high", "orders.priority.normal", "orders.priority.low"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class OrderEventListenerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ConsumerMetricsRegistry consumerMetricsRegistry;

    private KafkaTemplate<String, String> kafkaTemplate;

    @AfterEach
    void tearDown() {
        if (kafkaTemplate != null) {
            kafkaTemplate.destroy();
        }
    }

    @Test
    void shouldConsumeKafkaMessageAndPersistOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps()));
        sendWithRetry("orders.priority.high", "EU", """
                {
                  "orderId": "%s",
                  "eventId": "%s",
                  "customerId": "%s",
                  "region": "EU",
                  "amount": 123.45,
                  "priority": "HIGH",
                  "lines": [
                    {
                      "productId": "%s",
                      "quantity": 2,
                      "price": 61.73
                    }
                  ],
                  "emittedAt": "%s"
                }
                """.formatted(orderId, eventId, UUID.randomUUID(), UUID.randomUUID(), Instant.now()))
        ;

        Optional<Order> savedOrder = waitForOrder(eventId);

        assertTrue(savedOrder.isPresent());
        assertEquals(orderId, savedOrder.get().getOrderId());
        assertEquals(eventId, savedOrder.get().getEventId());
        assertEquals("EU", savedOrder.get().getRegion());
        assertEquals(new BigDecimal("123.45"), savedOrder.get().getAmount());
        assertEquals(OrderStatus.NEW, savedOrder.get().getStatus());
        assertEquals(1, consumerMetricsRegistry.getProcessedCount());
        assertEquals(0, consumerMetricsRegistry.getRejectedCount());
    }

    private Map<String, Object> producerProps() {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("delivery.timeout.ms", 5000);
        props.put("request.timeout.ms", 3000);
        props.put("retry.backoff.ms", 250);
        return props;
    }

    private void sendWithRetry(String topic, String key, String payload) throws Exception {
        Exception lastError = null;

        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                kafkaTemplate.send(topic, key, payload).get();
                return;
            } catch (Exception ex) {
                lastError = ex;
                Thread.sleep(500);
            }
        }

        throw lastError;
    }

    private Optional<Order> waitForOrder(UUID eventId) throws InterruptedException {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (Instant.now().isBefore(deadline)) {
            Optional<Order> order = orderRepository.findByEventId(eventId);
            if (order.isPresent()) {
                return order;
            }
            Thread.sleep(200);
        }
        return Optional.empty();
    }
}
