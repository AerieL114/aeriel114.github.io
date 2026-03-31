package com.example.orderservice.service;

import com.example.orderservice.dto.*;
import com.example.orderservice.exception.ProductNotInStockException;
import com.example.orderservice.model.*;
import com.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class OrderService {

    private static final String ORDER_TOPIC = "notificationTopic";
    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final DateTimeFormatter ORDER_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderService(
        OrderRepository orderRepository,
        InventoryClient inventoryClient,
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setStatus("PLACED");

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
            .stream()
            .map(this::mapToEntity)
            .collect(Collectors.toList());
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = orderLineItems.stream()
            .map(OrderLineItems::getSkuCode)
            .collect(Collectors.toList());

        InventoryResponse[] inventoryResponses;
        try {
            inventoryResponses = inventoryClient.getInventory(skuCodes).join();
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw ex;
        }

        boolean allInStock = inventoryResponses != null
            && List.of(inventoryResponses).stream().allMatch(InventoryResponse::isInStock);

        if (!allInStock) {
            throw new ProductNotInStockException("Product is not in stock, please try again later");
        }

        List<InventoryDeductionRequest> deductionRequests = orderLineItems.stream()
            .collect(Collectors.groupingBy(
                OrderLineItems::getSkuCode,
                Collectors.summingInt(OrderLineItems::getQuantity)
            ))
            .entrySet()
            .stream()
            .map(entry -> new InventoryDeductionRequest(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        try {
            inventoryClient.deductInventory(deductionRequests).join();
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw ex;
        }

        Order savedOrder = orderRepository.save(order);
        sendOrderCreatedEvent(savedOrder);
        sendNotification(savedOrder.getOrderNumber());
        return "Order Placed";
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        return toResponse(
            orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"))
        );
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(ORDER_NUMBER_FORMATTER);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderLineItemsDto> items = order.getOrderLineItemsList()
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            items,
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }

    private OrderLineItems mapToEntity(OrderLineItemsDto dto) {
        OrderLineItems items = new OrderLineItems();
        items.setProductName(resolveProductName(dto));
        items.setSkuCode(dto.getSkuCode());
        items.setPrice(dto.getPrice());
        items.setQuantity(dto.getQuantity());
        items.setSize(dto.getSize());
        items.setColor(dto.getColor());
        return items;
    }

    private OrderLineItemsDto mapToDto(OrderLineItems items) {
        OrderLineItemsDto dto = new OrderLineItemsDto();
        dto.setProductName(items.getProductName());
        dto.setSkuCode(items.getSkuCode());
        dto.setPrice(items.getPrice());
        dto.setQuantity(items.getQuantity());
        dto.setSize(items.getSize());
        dto.setColor(items.getColor());
        return dto;
    }

    private void sendNotification(String orderNumber) {
        NotificationEvent event = new NotificationEvent(orderNumber, "Order Placed Successfully");
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ORDER_TOPIC, payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize notification event", ex);
        }
    }

    private void sendOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setEventType("order.created");
        event.setOrderId(order.getId());
        event.setOrderNumber(order.getOrderNumber());
        event.setStatus(order.getStatus());
        event.setItems(order.getOrderLineItemsList().stream().map(this::mapToDto).collect(Collectors.toList()));
        event.setOccurredAt(LocalDateTime.now());

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize order created event", ex);
        }
    }

    private String resolveProductName(OrderLineItemsDto dto) {
        String productName = dto.getProductName();
        if (productName == null || productName.isBlank()) {
            return dto.getSkuCode();
        }
        return productName;
    }
}
