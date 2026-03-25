package com.example.orderservice.dto;

public class NotificationEvent {

    private String orderNumber;
    private String message;

    public NotificationEvent() {
    }

    public NotificationEvent(String orderNumber, String message) {
        this.orderNumber = orderNumber;
        this.message = message;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
