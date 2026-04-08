package ru.mentee.power.orders.domain.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.adapters.web.mapper.OrderEventMapper;
import ru.mentee.power.orders.command.PlaceOrderCommand;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceOrderUseCase implements PlaceOrderPort {
    private final OrderEventPort orderEventPort;
    private final OrderEventMapper orderEventMapper;

    @Override
    public void placeOrder(PlaceOrderCommand command) {
        OrderPlacedEvent event = orderEventMapper.toOrderPlacedEvent(command, UUID.randomUUID());
        orderEventPort.publish(event);
    }
}
