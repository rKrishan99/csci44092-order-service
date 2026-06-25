package com.csci44092.order.messaging;

import java.time.LocalDateTime;

/**
 * Event model published to RabbitMQ whenever an order is successfully created.
 * Contains the key data needed by downstream consumers (e.g., notification-service).
 */
public class OrderEvent {

    private Long orderId;
    private String customerId;
    private LocalDateTime timestamp;

    // Default constructor required for JSON deserialization
    public OrderEvent() {
    }

    public OrderEvent(Long orderId, String customerId, LocalDateTime timestamp) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.timestamp = timestamp;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "orderId=" + orderId +
                ", customerId='" + customerId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
