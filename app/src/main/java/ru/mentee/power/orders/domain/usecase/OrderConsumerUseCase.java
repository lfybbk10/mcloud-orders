package ru.mentee.power.orders.domain.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.orders.adapters.metrics.ConsumerMetricsRegistry;
import ru.mentee.power.orders.command.ProcessOrderCommand;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort;
import ru.mentee.power.orders.ports.outgoing.OrderPersistencePort;

@Component
@RequiredArgsConstructor
public class OrderConsumerUseCase implements ProcessOrderEventPort {
    private final OrderPersistencePort persistencePort;
    private final ConsumerMetricsRegistry consumerMetricsRegistry;

    @Override
    @Transactional
    public void process(ProcessOrderCommand command) {
        if (isInvalid(command)) {
            consumerMetricsRegistry.recordRejected();
            return;
        }

        if (persistencePort.existsByEventId(command.eventId())
                || persistencePort.existsByKafkaOffset(command.kafkaOffset())) {
            return;
        }

        persistencePort.save(command);
        consumerMetricsRegistry.recordProcessed(command.priority(), command.region());
    }

    private boolean isInvalid(ProcessOrderCommand command) {
        return command.orderId() == null
                || command.eventId() == null
                || command.priority() == null
                || command.kafkaOffset() == null
                || command.kafkaOffset().isBlank()
                || command.region() == null
                || command.region().isBlank();
    }
}
