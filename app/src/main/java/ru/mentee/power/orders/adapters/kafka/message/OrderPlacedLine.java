package ru.mentee.power.orders.adapters.kafka.message;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedLine(UUID productId, int quantity, BigDecimal price) {
}
