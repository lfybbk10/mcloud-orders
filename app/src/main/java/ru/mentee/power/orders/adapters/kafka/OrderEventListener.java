package ru.mentee.power.orders.adapters.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.adapters.web.mapper.ProcessOrderMapper;
import ru.mentee.power.orders.command.ProcessOrderCommand;
import ru.mentee.power.orders.ports.incoming.ProcessOrderPort;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ProcessOrderPort processOrderPort;
    private final ProcessOrderMapper processOrderMapper;

    @KafkaListener(
            topics = {"orders.priority.high", "orders.priority.normal", "orders.priority.low"},
            groupId = "orders-processor-group",
            containerFactory = "orderListenerFactory")
    public void listen(@Payload OrderPlacedEvent placedEvent, Acknowledgment ack) {
        ProcessOrderCommand processOrderCommand = processOrderMapper.toCommand(placedEvent);
        processOrderPort.process(processOrderCommand);
        if (ack != null) {
            ack.acknowledge();
        }
    }
}
