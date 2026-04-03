package ru.mentee.power.orders.exception;

public class OrderPublishException extends RuntimeException {
    public OrderPublishException(String message) {
        super(message);
    }

    public OrderPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
