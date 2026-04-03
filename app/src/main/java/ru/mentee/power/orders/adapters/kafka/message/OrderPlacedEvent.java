package ru.mentee.power.orders.adapters.kafka.message;

import ru.mentee.power.orders.domain.model.OrderPriority;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID customerId,
        String region,
        BigDecimal amount,
        OrderPriority priority,
        List<OrderPlacedLine> lines,
        Instant emittedAt
) {

    public OrderPlacedEvent(){
        this(UUID.randomUUID(), null, null, null, null, null);
    }
}

