package ru.mentee.power.orders.adapters.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mentee.power.orders.command.ProcessOrderCommand;
import ru.mentee.power.orders.domain.model.Order;

@Mapper(componentModel = "spring")
public interface OrderCommandMapper{

    @Mapping(target = "id", ignore = true)
    Order toOrder(ProcessOrderCommand processOrderCommand);
}
