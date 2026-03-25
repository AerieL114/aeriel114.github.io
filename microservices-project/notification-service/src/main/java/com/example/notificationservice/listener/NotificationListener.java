package com.example.notificationservice.listener;

import com.example.notificationservice.dto.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    private final ObjectMapper objectMapper;

    public NotificationListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "notificationTopic", groupId = "notification-service")
    public void handleMessage(String payload) {
        try {
            NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);
            logger.info("Received notification: orderNumber={}, message={}", event.getOrderNumber(), event.getMessage());
            simulateEmailSend(event);
        } catch (Exception ex) {
            logger.warn("Failed to parse notification payload: {}", payload, ex);
        }
    }

    private void simulateEmailSend(NotificationEvent event) {
        logger.info("Simulating email send for orderNumber={} with message={}", event.getOrderNumber(), event.getMessage());
    }
}
