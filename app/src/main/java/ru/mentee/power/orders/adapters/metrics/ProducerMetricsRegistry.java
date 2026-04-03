package ru.mentee.power.orders.adapters.metrics;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProducerMetricsRegistry {

    private final Map<String, AtomicInteger> successByTopic = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failureByTopic = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> successByRegion = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failureByRegion = new ConcurrentHashMap<>();

    public void recordSuccess(String topic, String region){
        increment(successByTopic, topic);
        increment(successByRegion, region);
    }

    public void recordFailure(String topic, String region){
        increment(failureByTopic, topic);
        increment(failureByRegion, region);
    }

    public int getTotalSuccess(){
        return successByTopic.values().stream()
                .mapToInt(AtomicInteger::intValue)
                .sum();
    }

    public int getTotalFailure(){
        return failureByTopic.values().stream()
                .mapToInt(AtomicInteger::intValue)
                .sum();
    }

    public int getSuccessByTopic(String topic){
        if(!successByTopic.containsKey(topic)){
            return 0;
        }
        return successByTopic.get(topic).intValue();
    }

    public int getFailureByTopic(String topic){
        if(!failureByTopic.containsKey(topic)){
            return 0;
        }
        return failureByTopic.get(topic).intValue();
    }

    public List<String> getAllTopics(){
        Stream<String> successTopics = successByTopic.keySet().stream();
        Stream<String> failureTopics = failureByTopic.keySet().stream();

        return Stream.concat(successTopics, failureTopics).distinct().collect(Collectors.toList());
    }

    private void increment(Map<String, AtomicInteger> counters, String key) {
        counters.computeIfAbsent(normalize(key), k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
