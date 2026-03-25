package com.example.notificationservice;

import com.example.notificationservice.listener.NotificationListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationListenerTest {

    @Test
    void handleMessageParsesJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        NotificationListener listener = new NotificationListener(objectMapper);

        String payload = "{\"orderNumber\":\"ORD123\",\"message\":\"Order Placed Successfully\"}";

        assertDoesNotThrow(() -> listener.handleMessage(payload));
    }
}
