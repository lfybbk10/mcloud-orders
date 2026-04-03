package ru.mentee.power.orders.adapters.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.command.PlaceOrderCommand;

@Mapper(componentModel = "spring")
public interface OrderEventMapper {

    @Mapping(source = "requestedAt", target = "emittedAt")
    OrderPlacedEvent toOrderPlacedEvent(PlaceOrderCommand orderPlacedEvent);
}
