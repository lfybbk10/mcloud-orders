package ru.mentee.power.orders.adapters.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.adapters.web.mapper.OrderCommandMapper;
import ru.mentee.power.orders.command.ProcessOrderCommand;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.ports.outgoing.OrderPersistencePort;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderPersistencePort {
    private final OrderRepository repository;
    private final OrderCommandMapper mapper;

    @Override
    public boolean existsByEventId(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    public void save(ProcessOrderCommand orderCommand) {
        Order order = mapper.toOrder(orderCommand);
        System.out.println("amount: "+order.getAmount());
        repository.save(order);
    }
}
