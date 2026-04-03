package ru.mentee.power.orders.command;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineCommand(UUID productId, int quantity, BigDecimal price) {
}
