package com.example.productservice;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createReadAndUpdateProducts() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        ProductRequest request = new ProductRequest();
        request.setName("iPhone 13");
        request.setDescription("iPhone 13");
        request.setPrice(java.math.BigDecimal.valueOf(1200));
        request.setSkuCode("ts001_black_m");
        request.setCategory("ao-thun");
        request.setSize("M");
        request.setColor("black");
        request.setImageUrl("/assets/products/ts001_black_m.jpg");

        String body = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/product"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> postResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.CREATED.value(), postResponse.statusCode());

        ProductResponse created = objectMapper.readValue(postResponse.body(), ProductResponse.class);
        assertNotNull(created.getId());

        HttpRequest getByIdRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/product/" + created.getId()))
            .GET()
            .build();

        HttpResponse<String> getByIdResponse = client.send(getByIdRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.OK.value(), getByIdResponse.statusCode());

        ProductResponse fetched = objectMapper.readValue(getByIdResponse.body(), ProductResponse.class);
        assertEquals(created.getId(), fetched.getId());
        assertEquals("iPhone 13", fetched.getName());

        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/product"))
            .GET()
            .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.OK.value(), getResponse.statusCode());

        ProductResponse[] products = objectMapper.readValue(getResponse.body(), ProductResponse[].class);
        assertTrue(products.length > 0);
        assertEquals("iPhone 13", products[0].getName());
        assertEquals("ts001_black_m", products[0].getSkuCode());

        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Ao thun oversize");
        updateRequest.setDescription("Ao thun oversize cotton");
        updateRequest.setPrice(java.math.BigDecimal.valueOf(249));
        updateRequest.setSkuCode("ts001_white_l");
        updateRequest.setCategory("ao-thun");
        updateRequest.setSize("L");
        updateRequest.setColor("white");
        updateRequest.setImageUrl("/assets/products/ts001_white_l.jpg");

        HttpRequest putRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/product/" + created.getId()))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updateRequest)))
            .build();

        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.OK.value(), putResponse.statusCode());

        ProductResponse updated = objectMapper.readValue(putResponse.body(), ProductResponse.class);
        assertEquals("Ao thun oversize", updated.getName());
        assertEquals("ts001_white_l", updated.getSkuCode());
        assertEquals("L", updated.getSize());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/product/" + created.getId()))
            .DELETE()
            .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse.statusCode());
    }
}
