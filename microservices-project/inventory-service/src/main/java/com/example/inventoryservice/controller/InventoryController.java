package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryDeductionRequest;
import com.example.inventoryservice.dto.InventoryItemResponse;
import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.service.InventoryService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryItemResponse> createInventory(@RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createInventory(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryItemResponse>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryItemResponse> getInventoryById(@PathVariable Long inventoryId) {
        return ResponseEntity.ok(inventoryService.getInventoryById(inventoryId));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> checkStock(@RequestParam List<String> skuCode) {
        return ResponseEntity.ok(inventoryService.checkStock(skuCode));
    }

    @PostMapping("/deduct")
    public ResponseEntity<Void> deductInventory(@RequestBody List<InventoryDeductionRequest> requests) {
        inventoryService.deductInventory(requests);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{inventoryId}")
    public ResponseEntity<InventoryItemResponse> updateInventory(
        @PathVariable Long inventoryId,
        @RequestBody InventoryRequest request
    ) {
        return ResponseEntity.ok(inventoryService.updateInventory(inventoryId, request));
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.noContent().build();
    }
}
