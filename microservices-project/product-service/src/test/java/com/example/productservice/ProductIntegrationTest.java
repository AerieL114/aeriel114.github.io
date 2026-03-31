package com.example.productservice;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.dto.ProductVariantRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.math.BigDecimal;
import java.util.List;
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
        request.setCategory("ao-thun");
        request.setVariants(List.of(buildVariant("ts001_black_m", BigDecimal.valueOf(1200), "M", "black")));

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
        assertEquals(1, products[0].getVariants().size());
        assertEquals("ts001_black_m", products[0].getVariants().get(0).getSkuCode());

        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Ao thun oversize");
        updateRequest.setDescription("Ao thun oversize cotton");
        updateRequest.setCategory("ao-thun");
        updateRequest.setVariants(List.of(buildVariant("ts001_white_l", BigDecimal.valueOf(249), "L", "white")));

        HttpRequest putRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/product/" + created.getId()))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updateRequest)))
            .build();

        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.OK.value(), putResponse.statusCode());

        ProductResponse updated = objectMapper.readValue(putResponse.body(), ProductResponse.class);
        assertEquals("Ao thun oversize", updated.getName());
        assertEquals("ts001_white_l", updated.getVariants().get(0).getSkuCode());
        assertEquals("L", updated.getVariants().get(0).getSize());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/product/" + created.getId()))
            .DELETE()
            .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse.statusCode());
    }

    private ProductVariantRequest buildVariant(String skuCode, BigDecimal price, String size, String color) {
        ProductVariantRequest variant = new ProductVariantRequest();
        variant.setSkuCode(skuCode);
        variant.setPrice(price);
        variant.setSize(size);
        variant.setColor(color);
        variant.setImageUrl("/assets/products/" + skuCode + ".jpg");
        return variant;
    }
}
