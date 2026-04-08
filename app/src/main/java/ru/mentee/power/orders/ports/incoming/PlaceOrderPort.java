package ru.mentee.power.orders.ports.incoming;

import ru.mentee.power.orders.command.PlaceOrderCommand;

import java.util.UUID;

public interface PlaceOrderPort {
    UUID placeOrder(PlaceOrderCommand command);


}
