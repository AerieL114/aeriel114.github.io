package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryDeductionRequest;
import com.example.inventoryservice.dto.InventoryItemResponse;
import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> checkStock(List<String> productCodes) {
        List<Inventory> inventoryItems = inventoryRepository.findBySkuCodeIn(productCodes);
        Map<String, Inventory> inventoryByProductCode = new HashMap<>();
        for (Inventory item : inventoryItems) {
            inventoryByProductCode.put(item.getSkuCode(), item);
        }

        return productCodes.stream()
            .map(productCode -> {
                Inventory inventory = inventoryByProductCode.get(productCode);
                boolean inStock = inventory != null && inventory.getQuantity() != null && inventory.getQuantity() > 0;
                return new InventoryResponse(productCode, inStock);
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getAllInventory() {
        return inventoryRepository.findAll()
            .stream()
            .map(this::toItemResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public InventoryItemResponse createInventory(InventoryRequest request) {
        Inventory inventory = new Inventory();
        inventory.setSkuCode(request.getSkuCode());
        inventory.setQuantity(request.getQuantity());
        return toItemResponse(inventoryRepository.save(inventory));
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryById(Long inventoryId) {
        return toItemResponse(getExistingInventory(inventoryId));
    }

    @Transactional
    public InventoryItemResponse updateInventory(Long inventoryId, InventoryRequest request) {
        Inventory inventory = getExistingInventory(inventoryId);
        inventory.setSkuCode(request.getSkuCode());
        inventory.setQuantity(request.getQuantity());
        return toItemResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public void deleteInventory(Long inventoryId) {
        inventoryRepository.delete(getExistingInventory(inventoryId));
    }

    @Transactional
    public void deductInventory(List<InventoryDeductionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No inventory deduction request provided");
        }

        Map<String, Integer> requestedQuantities = requests.stream()
            .collect(Collectors.groupingBy(
                InventoryDeductionRequest::getSkuCode,
                Collectors.summingInt(request -> safeQuantity(request.getQuantity()))
            ));

        List<String> skuCodes = requestedQuantities.keySet().stream()
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());

        List<Inventory> inventoryItems = inventoryRepository.findBySkuCodeIn(skuCodes);
        Map<String, Inventory> inventoryBySkuCode = inventoryItems.stream()
            .collect(Collectors.toMap(Inventory::getSkuCode, item -> item));

        for (String skuCode : skuCodes) {
            Inventory inventory = inventoryBySkuCode.get(skuCode);
            Integer requestedQuantity = requestedQuantities.get(skuCode);
            if (inventory == null || inventory.getQuantity() == null || inventory.getQuantity() < requestedQuantity) {
                throw new ResponseStatusException(BAD_REQUEST, "Insufficient inventory for skuCode: " + skuCode);
            }
        }

        inventoryItems.forEach(item -> item.setQuantity(item.getQuantity() - requestedQuantities.get(item.getSkuCode())));
        inventoryRepository.saveAll(inventoryItems);
    }

    private Inventory getExistingInventory(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Inventory not found"));
    }

    private InventoryItemResponse toItemResponse(Inventory item) {
        Integer quantity = item.getQuantity();
        boolean inStock = quantity != null && quantity > 0;
        return new InventoryItemResponse(
            item.getId(),
            item.getSkuCode(),
            quantity,
            inStock,
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }

    private int safeQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Quantity must be greater than 0");
        }
        return quantity;
    }
}
