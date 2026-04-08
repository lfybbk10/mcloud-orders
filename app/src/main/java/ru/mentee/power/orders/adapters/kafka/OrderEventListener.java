package ru.mentee.power.orders.adapters.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.adapters.metrics.ConsumerMetricsRegistry;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ProcessOrderEventPort processOrderEventPort;
    private final OrderEventMapper orderEventMapper;
    private final ConsumerMetricsRegistry consumerMetricsRegistry;

    @KafkaListener(
            topics = {"orders.priority.high", "orders.priority.normal", "orders.priority.low"},
            containerFactory = "orderListenerFactory")
    public void listen(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        try {
            String kafkaOffset = topic + ":" + partition + ":" + offset;
            processOrderEventPort.process(orderEventMapper.toCommand(payload, kafkaOffset));
            ack.acknowledge();
        } catch (IllegalArgumentException ex) {
            consumerMetricsRegistry.recordRejected();
            ack.acknowledge();
        }
    }
}
