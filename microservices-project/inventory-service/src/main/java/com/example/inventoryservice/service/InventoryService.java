package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryDeductionRequest;
import com.example.inventoryservice.dto.InventoryItemResponse;
import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryReservationActionRequest;
import com.example.inventoryservice.dto.InventoryReservationRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.dto.ReservationResponse;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.model.Reservation;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.repository.ReservationRepository;
import java.time.LocalDateTime;
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

    private static final int RESERVATION_TTL_HOURS = 24;

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(InventoryRepository inventoryRepository, ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
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
                boolean inStock = inventory != null && inventory.getQuantityAvailable() != null && inventory.getQuantityAvailable() > 0;
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
        applyRequest(inventory, request);
        return toItemResponse(inventoryRepository.save(inventory));
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryById(Long inventoryId) {
        return toItemResponse(getExistingInventory(inventoryId));
    }

    @Transactional
    public InventoryItemResponse updateInventory(Long inventoryId, InventoryRequest request) {
        Inventory inventory = getExistingInventory(inventoryId);
        applyRequest(inventory, request);
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

        List<Inventory> inventoryItems = inventoryRepository.findBySkuCodeIn(List.copyOf(requestedQuantities.keySet()));
        Map<String, Inventory> inventoryBySkuCode = inventoryItems.stream()
            .collect(Collectors.toMap(Inventory::getSkuCode, item -> item));

        for (Map.Entry<String, Integer> entry : requestedQuantities.entrySet()) {
            Inventory inventory = inventoryBySkuCode.get(entry.getKey());
            if (inventory == null || inventory.getQuantityAvailable() < entry.getValue()) {
                throw new ResponseStatusException(BAD_REQUEST, "Insufficient inventory for skuCode: " + entry.getKey());
            }
        }

        inventoryItems.forEach(item -> item.setQuantityOnHand(item.getQuantityOnHand() - requestedQuantities.get(item.getSkuCode())));
        inventoryRepository.saveAll(inventoryItems);
    }

    @Transactional
    public ReservationResponse reserveInventory(InventoryReservationRequest request) {
        validateReservationRequest(request);

        Inventory inventory = getInventoryBySkuCode(request.getSkuCode());
        int requestedQuantity = safeQuantity(request.getQuantity());
        if (inventory.getQuantityAvailable() < requestedQuantity) {
            throw new ResponseStatusException(BAD_REQUEST, "Insufficient inventory for skuCode: " + request.getSkuCode());
        }

        Reservation reservation = reservationRepository.findByCartIdAndSkuCode(request.getCartId(), request.getSkuCode())
            .orElseGet(Reservation::new);

        int previousQuantity = reservation.getQuantity() == null ? 0 : reservation.getQuantity();
        int delta = requestedQuantity - previousQuantity;
        if (delta > 0 && inventory.getQuantityAvailable() < delta) {
            throw new ResponseStatusException(BAD_REQUEST, "Insufficient inventory for skuCode: " + request.getSkuCode());
        }

        reservation.setCartId(request.getCartId());
        reservation.setSkuCode(request.getSkuCode());
        reservation.setQuantity(requestedQuantity);
        reservation.setExpiresAt(LocalDateTime.now().plusHours(RESERVATION_TTL_HOURS));

        inventory.setQuantityReserved(inventory.getQuantityReserved() + delta);
        inventoryRepository.save(inventory);

        Reservation saved = reservationRepository.save(reservation);
        return new ReservationResponse(saved.getId(), saved.getCartId(), saved.getSkuCode(), saved.getQuantity(), saved.getExpiresAt());
    }

    @Transactional
    public void confirmReservation(InventoryReservationActionRequest request) {
        List<Reservation> reservations = getReservationsByCartId(request);
        Map<String, Integer> reservedBySku = reservations.stream()
            .collect(Collectors.groupingBy(Reservation::getSkuCode, Collectors.summingInt(Reservation::getQuantity)));

        List<Inventory> inventoryItems = inventoryRepository.findBySkuCodeIn(List.copyOf(reservedBySku.keySet()));
        for (Inventory inventory : inventoryItems) {
            int quantity = reservedBySku.getOrDefault(inventory.getSkuCode(), 0);
            inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
            inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
        }

        inventoryRepository.saveAll(inventoryItems);
        reservationRepository.deleteAll(reservations);
    }

    @Transactional
    public void releaseReservation(InventoryReservationActionRequest request) {
        List<Reservation> reservations = getReservationsByCartId(request);
        Map<String, Integer> reservedBySku = reservations.stream()
            .collect(Collectors.groupingBy(Reservation::getSkuCode, Collectors.summingInt(Reservation::getQuantity)));

        List<Inventory> inventoryItems = inventoryRepository.findBySkuCodeIn(List.copyOf(reservedBySku.keySet()));
        for (Inventory inventory : inventoryItems) {
            int quantity = reservedBySku.getOrDefault(inventory.getSkuCode(), 0);
            inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
        }

        inventoryRepository.saveAll(inventoryItems);
        reservationRepository.deleteAll(reservations);
    }

    private Inventory getExistingInventory(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Inventory not found"));
    }

    private Inventory getInventoryBySkuCode(String skuCode) {
        return inventoryRepository.findBySkuCode(skuCode)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Inventory not found for skuCode: " + skuCode));
    }

    private void applyRequest(Inventory inventory, InventoryRequest request) {
        inventory.setSkuCode(request.getSkuCode());
        inventory.setWarehouseId(defaultIfBlank(request.getWarehouseId(), "default"));
        inventory.setQuantityOnHand(safeNullableQuantity(request.getQuantity()));
        if (inventory.getQuantityReserved() == null) {
            inventory.setQuantityReserved(0);
        }
        inventory.setReorderPoint(request.getReorderPoint() == null ? 10 : request.getReorderPoint());
        inventory.setReorderQuantity(request.getReorderQuantity() == null ? 50 : request.getReorderQuantity());
    }

    private InventoryItemResponse toItemResponse(Inventory item) {
        Integer quantityOnHand = item.getQuantityOnHand();
        Integer quantityReserved = item.getQuantityReserved();
        Integer quantityAvailable = item.getQuantityAvailable();
        boolean inStock = quantityAvailable != null && quantityAvailable > 0;
        return new InventoryItemResponse(
            item.getId(),
            item.getSkuCode(),
            quantityOnHand,
            item.getWarehouseId(),
            quantityOnHand,
            quantityReserved,
            quantityAvailable,
            item.getReorderPoint(),
            item.getReorderQuantity(),
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

    private int safeNullableQuantity(Integer quantity) {
        if (quantity == null) {
            return 0;
        }
        if (quantity < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Quantity must not be negative");
        }
        return quantity;
    }

    private void validateReservationRequest(InventoryReservationRequest request) {
        if (request == null || request.getCartId() == null || request.getCartId().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "cartId is required");
        }
        if (request.getSkuCode() == null || request.getSkuCode().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "skuCode is required");
        }
        safeQuantity(request.getQuantity());
    }

    private List<Reservation> getReservationsByCartId(InventoryReservationActionRequest request) {
        if (request == null || request.getCartId() == null || request.getCartId().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "cartId is required");
        }
        List<Reservation> reservations = reservationRepository.findByCartId(request.getCartId());
        if (reservations.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Reservation not found for cartId: " + request.getCartId());
        }
        return reservations;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
