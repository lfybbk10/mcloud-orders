package ru.mentee.power.orders.adapters.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.command.OrderLineCommand;
import ru.mentee.power.orders.command.ProcessOrderCommand;
import ru.mentee.power.orders.domain.model.OrderStatus;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderEventMapper {

    private final ObjectMapper objectMapper;

    public ProcessOrderCommand toCommand(String payload, String kafkaOffset) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(payload, OrderPlacedEvent.class);
            return new ProcessOrderCommand(
                    event.orderId(),
                    event.eventId(),
                    kafkaOffset,
                    event.customerId(),
                    event.region(),
                    event.amount(),
                    event.priority(),
                    OrderStatus.NEW,
                    mapLines(event),
                    event.emittedAt()
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid order event payload", ex);
        }
    }

    private List<OrderLineCommand> mapLines(OrderPlacedEvent event) {
        if (event.lines() == null) {
            return List.of();
        }

        return event.lines().stream()
                .map(line -> new OrderLineCommand(line.productId(), line.quantity(), line.price()))
                .toList();
    }
}
