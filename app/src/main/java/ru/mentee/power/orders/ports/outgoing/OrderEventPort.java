package ru.mentee.power.orders.ports.outgoing;

import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;

import java.util.concurrent.CompletableFuture;

public interface OrderEventPort {
    void publish(OrderPlacedEvent event);
}
