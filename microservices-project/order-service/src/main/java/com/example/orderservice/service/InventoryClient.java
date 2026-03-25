package com.example.orderservice.service;

import com.example.orderservice.dto.InventoryDeductionRequest;
import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.exception.InventoryUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class InventoryClient {

    private static final String INVENTORY_SERVICE = "inventory";

    private final WebClient.Builder webClientBuilder;

    public InventoryClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @CircuitBreaker(name = INVENTORY_SERVICE, fallbackMethod = "inventoryFallback")
    @Retry(name = INVENTORY_SERVICE, fallbackMethod = "inventoryFallback")
    @TimeLimiter(name = INVENTORY_SERVICE, fallbackMethod = "inventoryFallback")
    public CompletableFuture<InventoryResponse[]> getInventory(List<String> skuCodes) {
        return webClientBuilder.build()
            .get()
            .uri("http://inventory-service/api/inventory", uriBuilder -> {
                for (String skuCode : skuCodes) {
                    uriBuilder.queryParam("skuCode", skuCode);
                }
                return uriBuilder.build();
            })
            .retrieve()
            .bodyToMono(InventoryResponse[].class)
            .toFuture();
    }

    @CircuitBreaker(name = INVENTORY_SERVICE, fallbackMethod = "deductInventoryFallback")
    @Retry(name = INVENTORY_SERVICE, fallbackMethod = "deductInventoryFallback")
    @TimeLimiter(name = INVENTORY_SERVICE, fallbackMethod = "deductInventoryFallback")
    public CompletableFuture<Void> deductInventory(List<InventoryDeductionRequest> requests) {
        return webClientBuilder.build()
            .post()
            .uri("http://inventory-service/api/inventory/deduct")
            .bodyValue(requests)
            .retrieve()
            .bodyToMono(Void.class)
            .toFuture();
    }

    private CompletableFuture<InventoryResponse[]> inventoryFallback(List<String> skuCodes, Throwable throwable) {
        return CompletableFuture.failedFuture(new InventoryUnavailableException("Inventory service is unavailable", throwable));
    }

    private CompletableFuture<Void> deductInventoryFallback(List<InventoryDeductionRequest> requests, Throwable throwable) {
        return CompletableFuture.failedFuture(new InventoryUnavailableException("Inventory service is unavailable", throwable));
    }
}
