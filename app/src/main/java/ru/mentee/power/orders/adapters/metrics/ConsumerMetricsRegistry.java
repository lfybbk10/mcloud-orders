package ru.mentee.power.orders.adapters.metrics;

import org.springframework.stereotype.Component;
import ru.mentee.power.orders.domain.model.OrderPriority;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ConsumerMetricsRegistry {

    private final Map<String, AtomicInteger> processedByPriority = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> processedByRegion = new ConcurrentHashMap<>();
    private final AtomicInteger rejectedCount = new AtomicInteger();

    public void recordProcessed(OrderPriority priority, String region) {
        increment(processedByPriority, priority == null ? null : priority.name());
        increment(processedByRegion, region);
    }

    public void recordRejected() {
        rejectedCount.incrementAndGet();
    }

    public int getRejectedCount() {
        return rejectedCount.get();
    }

    public int getProcessedCount() {
        return processedByPriority.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    public Map<String, Integer> getProcessedByPriority() {
        return snapshot(processedByPriority);
    }

    public Map<String, Integer> getProcessedByRegion() {
        return snapshot(processedByRegion);
    }

    private void increment(Map<String, AtomicInteger> counters, String key) {
        counters.computeIfAbsent(normalize(key), ignored -> new AtomicInteger())
                .incrementAndGet();
    }

    private Map<String, Integer> snapshot(Map<String, AtomicInteger> source) {
        Map<String, Integer> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, value.get()));
        return copy;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
