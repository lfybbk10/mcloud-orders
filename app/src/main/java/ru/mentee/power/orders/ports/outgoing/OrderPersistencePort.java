package ru.mentee.power.orders.ports.outgoing;

import ru.mentee.power.orders.command.ProcessOrderCommand;

import java.util.UUID;

public interface OrderPersistencePort {
    boolean existsByEventId(UUID eventId);
    boolean existsByKafkaOffset(String kafkaOffset);

    void save(ProcessOrderCommand orderCommand);
}
