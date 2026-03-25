package com.example.orderservice.service;

import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.dto.OrderLineItemsDto;
import com.example.orderservice.dto.OrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.orderservice.exception.ProductNotInStockException;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrderSuccess() throws Exception {
        InventoryResponse response = new InventoryResponse();
        response.setSkuCode("iphone_13");
        response.setInStock(true);

        when(inventoryClient.getInventory(anyList())).thenReturn(CompletableFuture.completedFuture(new InventoryResponse[]{response}));
        when(inventoryClient.deductInventory(anyList())).thenReturn(CompletableFuture.completedFuture(null));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"Order Placed Successfully\"}");

        OrderRequest request = new OrderRequest();
        OrderLineItemsDto item = new OrderLineItemsDto();
        item.setSkuCode("iphone_13");
        item.setPrice(BigDecimal.valueOf(1200));
        item.setQuantity(1);
        request.setOrderLineItemsDtoList(List.of(item));

        String result = orderService.placeOrder(request);

        assertEquals("Order Placed", result);
        verify(inventoryClient).deductInventory(anyList());
        verify(kafkaTemplate).send(eq("notificationTopic"), contains("Order Placed Successfully"));
    }

    @Test
    void placeOrderOutOfStock() {
        InventoryResponse response = new InventoryResponse();
        response.setSkuCode("iphone_13");
        response.setInStock(false);

        when(inventoryClient.getInventory(anyList())).thenReturn(CompletableFuture.completedFuture(new InventoryResponse[]{response}));

        OrderRequest request = new OrderRequest();
        OrderLineItemsDto item = new OrderLineItemsDto();
        item.setSkuCode("iphone_13");
        item.setPrice(BigDecimal.valueOf(1200));
        item.setQuantity(1);
        request.setOrderLineItemsDtoList(List.of(item));

        assertThrows(ProductNotInStockException.class, () -> orderService.placeOrder(request));
    }
}
