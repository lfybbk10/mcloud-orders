package ru.mentee.power.orders.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.adapters.web.mapper.OrderEventMapper;
import ru.mentee.power.orders.command.OrderLineCommand;
import ru.mentee.power.orders.command.PlaceOrderCommand;
import ru.mentee.power.orders.domain.model.OrderPriority;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceOrderUseCaseTest {

    @Mock
    private OrderEventPort orderEventPort;

    @Mock
    private OrderEventMapper orderEventMapper;

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

        var event = new OrderPlacedEvent();

        when(orderEventMapper.toOrderPlacedEvent(any())).thenReturn(event);

        placeOrderUseCase.placeOrder(command);

        verify(orderEventPort).publish(event);
    }
}
