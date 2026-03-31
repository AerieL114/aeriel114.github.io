package com.example.productservice.service;

import com.example.productservice.dto.ProductEvent;
import com.example.productservice.dto.ProductVariantResponse;
import com.example.productservice.model.Product;
import com.example.productservice.model.ProductVariant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductEventPublisher {

    private static final String PRODUCT_EVENTS_TOPIC = "product-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProductEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishProductCreated(Product product) {
        publish("product.created", product);
    }

    public void publishProductUpdated(Product product) {
        publish("product.updated", product);
    }

    private void publish(String eventType, Product product) {
        ProductEvent event = new ProductEvent();
        event.setEventType(eventType);
        event.setProductId(product.getId());
        event.setName(product.getName());
        event.setDescription(product.getDescription());
        event.setCategory(product.getCategory());
        event.setVariants(mapVariants(product));

        ProductVariant primaryVariant = getPrimaryVariant(product);
        if (primaryVariant != null) {
            event.setSkuCode(primaryVariant.getSkuCode());
            event.setPrice(primaryVariant.getPrice());
            event.setSize(primaryVariant.getSize());
            event.setColor(primaryVariant.getColor());
            event.setImageUrl(primaryVariant.getImageUrl());
        }
        event.setOccurredAt(LocalDateTime.now());

        try {
            kafkaTemplate.send(PRODUCT_EVENTS_TOPIC, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize product event", ex);
        }
    }

    private List<ProductVariantResponse> mapVariants(Product product) {
        return product.getVariants().stream()
            .map(this::toVariantResponse)
            .toList();
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
        return new ProductVariantResponse(
            variant.getId(),
            variant.getSkuCode(),
            variant.getSize(),
            variant.getColor(),
            variant.getImageUrl(),
            variant.getPrice(),
            variant.getCreatedAt(),
            variant.getUpdatedAt()
        );
    }

    private ProductVariant getPrimaryVariant(Product product) {
        return product.getVariants().stream().findFirst().orElse(null);
    }
}
