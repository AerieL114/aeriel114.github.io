package com.example.orderservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderCreatedEvent {

    private String eventType;
    private Long orderId;
    private String orderNumber;
    private String status;
    private List<OrderLineItemsDto> items;
    private LocalDateTime occurredAt;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderLineItemsDto> getItems() {
        return items;
    }

    public void setItems(List<OrderLineItemsDto> items) {
        this.items = items;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
