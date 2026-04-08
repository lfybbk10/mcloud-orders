package ru.mentee.power.orders.domain.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.orders.command.ProcessOrderCommand;
import ru.mentee.power.orders.ports.incoming.ProcessOrderPort;
import ru.mentee.power.orders.ports.outgoing.OrderPersistencePort;

@Component
@RequiredArgsConstructor
public class OrderConsumerUseCase implements ProcessOrderPort {
    private final OrderPersistencePort persistencePort;

    @Override
    @Transactional
    public void process(ProcessOrderCommand command) {
        if(persistencePort.existsByEventId(command.eventId())){
            return;
        }

        persistencePort.save(command);
    }
}
