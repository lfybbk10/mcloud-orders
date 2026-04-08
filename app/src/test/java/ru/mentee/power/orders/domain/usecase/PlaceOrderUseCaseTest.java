package ru.mentee.power.orders.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.adapters.web.mapper.OrderPublishEventMapper;
import ru.mentee.power.orders.command.OrderLineCommand;
import ru.mentee.power.orders.command.PlaceOrderCommand;
import ru.mentee.power.orders.domain.model.OrderPriority;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceOrderUseCaseTest {

    @Mock
    private OrderEventPort orderEventPort;

    @Mock
    private OrderPublishEventMapper orderPublishEventMapper;

    @InjectMocks
    private PlaceOrderUseCase placeOrderUseCase;

    @Test
    void shouldPublishEventWhenCommandIsValid() {

        var command = new PlaceOrderCommand(
                UUID.randomUUID(),
                "EU",
                new BigDecimal("100.00"),
                OrderPriority.HIGH,
                List.of(new OrderLineCommand(UUID.randomUUID(), 1, new BigDecimal("100"))),
                Instant.now()
        );

        when(orderPublishEventMapper.toOrderPlacedEvent(any(PlaceOrderCommand.class), any(UUID.class), any(UUID.class)))
                .thenAnswer(invocation -> new OrderPlacedEvent(
                        invocation.getArgument(1, UUID.class),
                        invocation.getArgument(2, UUID.class),
                        command.customerId(),
                        command.region(),
                        command.amount(),
                        command.priority(),
                        List.of(),
                        command.requestedAt()
                ));

        UUID result = placeOrderUseCase.placeOrder(command);

        var eventCaptor = forClass(OrderPlacedEvent.class);
        verify(orderEventPort).publish(eventCaptor.capture());
        assertEquals(result, eventCaptor.getValue().orderId());
    }
}
