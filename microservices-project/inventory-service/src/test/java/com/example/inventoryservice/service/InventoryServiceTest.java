package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryItemResponse;
import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void checkStock() {
        Inventory inStock = new Inventory();
        inStock.setSkuCode("iphone_13");
        inStock.setQuantity(10);

        Inventory outOfStock = new Inventory();
        outOfStock.setSkuCode("iphone_13_red");
        outOfStock.setQuantity(0);

        when(inventoryRepository.findBySkuCodeIn(List.of("iphone_13", "iphone_13_red")))
            .thenReturn(List.of(inStock, outOfStock));

        List<InventoryResponse> responses = inventoryService.checkStock(List.of("iphone_13", "iphone_13_red"));

        assertEquals(2, responses.size());
        assertEquals("iphone_13", responses.get(0).getSkuCode());
        assertEquals(true, responses.get(0).isInStock());
        assertEquals("iphone_13_red", responses.get(1).getSkuCode());
        assertEquals(false, responses.get(1).isInStock());
    }

    @Test
    void createInventory() {
        InventoryRequest request = new InventoryRequest();
        request.setSkuCode("ts001_black_m");
        request.setQuantity(15);

        Inventory saved = new Inventory();
        saved.setId(1L);
        saved.setSkuCode(request.getSkuCode());
        saved.setQuantity(request.getQuantity());

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(saved);

        InventoryItemResponse response = inventoryService.createInventory(request);

        assertEquals(1L, response.getId());
        assertEquals("ts001_black_m", response.getSkuCode());
        assertEquals(15, response.getQuantity());
        assertEquals(true, response.isInStock());
    }

    @Test
    void updateInventory() {
        Inventory existing = new Inventory();
        existing.setId(1L);
        existing.setSkuCode("ts001_black_m");
        existing.setQuantity(10);

        InventoryRequest request = new InventoryRequest();
        request.setSkuCode("ts001_black_m");
        request.setQuantity(5);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItemResponse response = inventoryService.updateInventory(1L, request);

        assertEquals(1L, response.getId());
        assertEquals(5, response.getQuantity());
        assertEquals(true, response.isInStock());
    }

    @Test
    void deleteInventory() {
        Inventory existing = new Inventory();
        existing.setId(1L);
        existing.setSkuCode("ts001_black_m");
        existing.setQuantity(10);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        inventoryService.deleteInventory(1L);

        verify(inventoryRepository).delete(existing);
    }
}
