package ru.mentee.power.orders.adapters.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mentee.power.orders.command.PlaceOrderCommand;
import ru.mentee.power.orders.model.OrderRequest;

@Mapper(componentModel = "spring")
public interface OrderRequestMapper {

    @Mapping(target = "requestedAt", expression = "java(java.time.Instant.now())")
    PlaceOrderCommand toCommand(OrderRequest orderRequest);
}
