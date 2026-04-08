package ru.mentee.power.orders.command;

import ru.mentee.power.orders.domain.model.OrderPriority;
import ru.mentee.power.orders.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProcessOrderCommand(
        UUID orderId,
        UUID eventId,
        String kafkaOffset,
        UUID customerId,
        String region,
        BigDecimal amount,
        OrderPriority priority,
        OrderStatus status,
        List<OrderLineCommand> lines,
        Instant createdAt
) {

}
