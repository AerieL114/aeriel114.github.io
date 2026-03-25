package com.example.orderservice;

import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.dto.OrderLineItemsDto;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.service.InventoryClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(OrderIntegrationTest.TestConfig.class)
class OrderIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void placeOrderReturnsCreated() throws Exception {
        InventoryResponse response = new InventoryResponse();
        response.setSkuCode("ts001_black_m");
        response.setInStock(true);
        when(inventoryClient.getInventory(anyList())).thenReturn(CompletableFuture.completedFuture(new InventoryResponse[]{response}));
        when(inventoryClient.deductInventory(anyList())).thenReturn(CompletableFuture.completedFuture(null));

        OrderLineItemsDto item = new OrderLineItemsDto();
        item.setSkuCode("ts001_black_m");
        item.setPrice(java.math.BigDecimal.valueOf(199));
        item.setQuantity(1);
        item.setSize("M");
        item.setColor("black");

        OrderRequest request = new OrderRequest();
        request.setOrderLineItemsDtoList(List.of(item));

        String body = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/order"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> responseEntity = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.CREATED.value(), responseEntity.statusCode());
        assertEquals("Order Placed", responseEntity.body());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        InventoryClient inventoryClient() {
            return Mockito.mock(InventoryClient.class);
        }

        @Bean
        @Primary
        KafkaTemplate<String, String> kafkaTemplate() {
            return Mockito.mock(KafkaTemplate.class);
        }
    }
}
