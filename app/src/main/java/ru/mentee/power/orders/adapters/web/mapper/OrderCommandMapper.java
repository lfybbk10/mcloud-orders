package ru.mentee.power.orders.adapters.web.mapper;

import org.mapstruct.Mapper;
import ru.mentee.power.orders.command.ProcessOrderCommand;
import ru.mentee.power.orders.domain.model.Order;

@Mapper(componentModel = "spring")
public interface OrderCommandMapper{

    Order toOrder(ProcessOrderCommand processOrderCommand);
}
