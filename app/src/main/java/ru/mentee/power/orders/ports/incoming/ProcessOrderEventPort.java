package ru.mentee.power.orders.ports.incoming;

import ru.mentee.power.orders.command.ProcessOrderCommand;

public interface ProcessOrderEventPort {

    void process(ProcessOrderCommand command);
}
