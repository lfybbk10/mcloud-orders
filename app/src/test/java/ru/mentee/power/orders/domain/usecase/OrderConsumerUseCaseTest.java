package ru.mentee.power.orders.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.orders.adapters.metrics.ConsumerMetricsRegistry;
import ru.mentee.power.orders.command.ProcessOrderCommand;
import ru.mentee.power.orders.domain.model.OrderPriority;
import ru.mentee.power.orders.domain.model.OrderStatus;
import ru.mentee.power.orders.ports.outgoing.OrderPersistencePort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderConsumerUseCaseTest {

    @Mock
    private OrderPersistencePort persistencePort;

    @Mock
    private ConsumerMetricsRegistry consumerMetricsRegistry;

    @InjectMocks
    private OrderConsumerUseCase orderConsumerUseCase;

    @Test
    void shouldSaveOrderWhenEventIsNew() {
        ProcessOrderCommand command = command("EU", OrderPriority.HIGH);
        when(persistencePort.existsByEventId(command.eventId())).thenReturn(false);
        when(persistencePort.existsByKafkaOffset(command.kafkaOffset())).thenReturn(false);

        orderConsumerUseCase.process(command);

        verify(persistencePort).save(command);
        verify(consumerMetricsRegistry).recordProcessed(OrderPriority.HIGH, "EU");
        verify(consumerMetricsRegistry, never()).recordRejected();
    }

    @Test
    void shouldRejectEventWithMissingRegion() {
        ProcessOrderCommand command = command(null, OrderPriority.HIGH);

        orderConsumerUseCase.process(command);

        verify(consumerMetricsRegistry).recordRejected();
        verify(persistencePort, never()).save(command);
    }

    @Test
    void shouldIgnoreDuplicateOrderEvent() {
        ProcessOrderCommand command = command("EU", OrderPriority.NORMAL);
        when(persistencePort.existsByEventId(command.eventId())).thenReturn(true);

        orderConsumerUseCase.process(command);

        verify(persistencePort, never()).save(command);
        verify(consumerMetricsRegistry, never()).recordProcessed(command.priority(), command.region());
        verify(consumerMetricsRegistry, never()).recordRejected();
    }

    private ProcessOrderCommand command(String region, OrderPriority priority) {
        return new ProcessOrderCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "orders.priority.high:0:42",
                UUID.randomUUID(),
                region,
                new BigDecimal("100.00"),
                priority,
                OrderStatus.NEW,
                List.of(),
                Instant.now()
        );
    }
}
