package ru.mentee.power.orders.adapters.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mentee.power.orders.adapters.kafka.message.OrderPlacedEvent;
import ru.mentee.power.orders.command.ProcessOrderCommand;

@Mapper(componentModel = "spring")
public interface ProcessOrderMapper {

    @Mapping(source = "emittedAt", target = "createdAt")
    ProcessOrderCommand toCommand(OrderPlacedEvent event);
}
