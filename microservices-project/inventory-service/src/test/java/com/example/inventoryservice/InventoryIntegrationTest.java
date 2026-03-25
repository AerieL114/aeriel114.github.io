package com.example.inventoryservice;

import com.example.inventoryservice.dto.InventoryItemResponse;
import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        inventoryRepository.deleteAll();

        Inventory inStock = new Inventory();
        inStock.setSkuCode("iphone_13");
        inStock.setQuantity(10);

        Inventory outOfStock = new Inventory();
        outOfStock.setSkuCode("iphone_13_red");
        outOfStock.setQuantity(0);

        inventoryRepository.save(inStock);
        inventoryRepository.save(outOfStock);
    }

    @Test
    void checkStock() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/inventory?skuCode=iphone_13&skuCode=iphone_13_red"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.OK.value(), response.statusCode());

        InventoryResponse[] results = objectMapper.readValue(response.body(), InventoryResponse[].class);
        assertNotNull(results);
        assertEquals("iphone_13", results[0].getSkuCode());
        assertEquals(true, results[0].isInStock());
        assertEquals("iphone_13_red", results[1].getSkuCode());
        assertEquals(false, results[1].isInStock());
    }

    @Test
    void createReadUpdateAndDeleteInventory() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        InventoryRequest request = new InventoryRequest();
        request.setSkuCode("ts001_black_m");
        request.setQuantity(12);

        HttpRequest createRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/inventory"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
            .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.CREATED.value(), createResponse.statusCode());

        InventoryItemResponse created = objectMapper.readValue(createResponse.body(), InventoryItemResponse.class);
        assertNotNull(created.getId());
        assertEquals("ts001_black_m", created.getSkuCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/inventory/" + created.getId()))
            .GET()
            .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.OK.value(), getResponse.statusCode());

        InventoryRequest updateRequest = new InventoryRequest();
        updateRequest.setSkuCode("ts001_black_m");
        updateRequest.setQuantity(0);

        HttpRequest putRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/inventory/" + created.getId()))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updateRequest)))
            .build();

        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.OK.value(), putResponse.statusCode());

        InventoryItemResponse updated = objectMapper.readValue(putResponse.body(), InventoryItemResponse.class);
        assertEquals(0, updated.getQuantity());
        assertEquals(false, updated.isInStock());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/inventory/" + created.getId()))
            .DELETE()
            .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse.statusCode());
    }
}
