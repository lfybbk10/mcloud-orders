package ru.mentee.power.orders.adapters.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.orders.adapters.metrics.ConsumerMetricsRegistry;
import ru.mentee.power.orders.adapters.metrics.ProducerMetricsRegistry;
import ru.mentee.power.orders.adapters.web.mapper.OrderRequestMapper;
import ru.mentee.power.orders.api.OrdersApi;
import ru.mentee.power.orders.model.*;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;

import java.time.OffsetDateTime;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final PlaceOrderPort placeOrderPort;
    private final OrderRequestMapper orderRequestMapper;

    private final ProducerMetricsRegistry producerMetricsRegistry;
    private final ConsumerMetricsRegistry consumerMetricsRegistry;

    @Override
    public ResponseEntity<OrderAcceptedResponse> submitOrder(OrderRequest orderRequest) {
        var orderId = placeOrderPort.placeOrder(orderRequestMapper.toCommand(orderRequest));
        OrderAcceptedResponse orderAcceptedResponse = new OrderAcceptedResponse(orderId, OrderAcceptedResponse.StatusEnum.QUEUED, OffsetDateTime.now());
        return ResponseEntity.accepted().body(orderAcceptedResponse);
    }

    @Override
    public ResponseEntity<ProducerMetricsResponse> getOrderProducerMetrics() {
        ProducerMetricsResponse producerMetricsResponse = new ProducerMetricsResponse();

        ProducerMetricsResponseTotals producerMetricsResponseTotals = new ProducerMetricsResponseTotals();
        producerMetricsResponseTotals.success(producerMetricsRegistry.getTotalSuccess());
        producerMetricsResponseTotals.failure(producerMetricsRegistry.getTotalFailure());

        producerMetricsResponse.setTotals(producerMetricsResponseTotals);

        HashMap<String, ProducerMetricsResponseTopicsValue> producerMetricsResponseTopicsValues = new HashMap<>();
        for (String topic : producerMetricsRegistry.getAllTopics()) {
            ProducerMetricsResponseTopicsValue producerMetricsResponseTopicsValue = new ProducerMetricsResponseTopicsValue();
            producerMetricsResponseTopicsValue.success(producerMetricsRegistry.getSuccessByTopic(topic));
            producerMetricsResponseTopicsValue.failure(producerMetricsRegistry.getFailureByTopic(topic));
            producerMetricsResponseTopicsValues.put(topic, producerMetricsResponseTopicsValue);
        }

        producerMetricsResponse.topics(producerMetricsResponseTopicsValues);
        return ResponseEntity.ok(producerMetricsResponse);
    }

    @Override
    public ResponseEntity<ConsumerMetricsResponse> getOrderConsumerMetrics() {
        ConsumerMetricsResponse response = new ConsumerMetricsResponse();
        ConsumerMetricsResponseTotals totals = new ConsumerMetricsResponseTotals();
        totals.processed(consumerMetricsRegistry.getProcessedCount());
        totals.rejected(consumerMetricsRegistry.getRejectedCount());
        response.setTotals(totals);
        response.setPriorities(consumerMetricsRegistry.getProcessedByPriority());
        response.setRegions(consumerMetricsRegistry.getProcessedByRegion());
        return ResponseEntity.ok(response);
    }
}
