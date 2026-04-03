package ru.mentee.power.orders.ports.incoming;

import ru.mentee.power.orders.command.PlaceOrderCommand;

public interface PlaceOrderPort {
    void placeOrder(PlaceOrderCommand command);


}
