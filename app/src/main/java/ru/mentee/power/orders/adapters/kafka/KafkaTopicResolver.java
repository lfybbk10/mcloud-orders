package ru.mentee.power.orders.adapters.kafka;

import org.springframework.stereotype.Component;
import ru.mentee.power.orders.domain.model.OrderPriority;

@Component
public class KafkaTopicResolver {

    public String resolveTopic(OrderPriority priority) {
        return switch (priority) {
            case HIGH -> "orders.priority.high";
            case NORMAL -> "orders.priority.normal";
            case LOW -> "orders.priority.low";
        };
    }
}
