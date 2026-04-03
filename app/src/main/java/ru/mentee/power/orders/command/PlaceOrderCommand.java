package ru.mentee.power.orders.command;

import ru.mentee.power.orders.domain.model.OrderPriority;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaceOrderCommand(
        UUID customerId,
        String region,
        BigDecimal amount,
        OrderPriority priority,
        List<OrderLineCommand> lines,
        Instant requestedAt
) {
}