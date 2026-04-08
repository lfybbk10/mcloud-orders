package ru.mentee.power.orders.adapters.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.command.PlaceOrderCommand;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderPublishEventMapper {

    @Mapping(source = "orderPlacedEvent.requestedAt", target = "emittedAt")
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "eventId", source = "eventId")
    OrderPlacedEvent toOrderPlacedEvent(PlaceOrderCommand orderPlacedEvent, UUID orderId, UUID eventId);
}
