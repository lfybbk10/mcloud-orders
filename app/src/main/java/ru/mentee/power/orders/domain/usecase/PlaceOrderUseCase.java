package ru.mentee.power.orders.domain.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.adapters.web.mapper.OrderPublishEventMapper;
import ru.mentee.power.orders.command.PlaceOrderCommand;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceOrderUseCase implements PlaceOrderPort {
    private final OrderEventPort orderEventPort;
    private final OrderPublishEventMapper orderPublishEventMapper;

    @Override
    public UUID placeOrder(PlaceOrderCommand command) {
        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        OrderPlacedEvent event = orderPublishEventMapper.toOrderPlacedEvent(command, orderId, eventId);
        orderEventPort.publish(event);
        return orderId;
    }
}
