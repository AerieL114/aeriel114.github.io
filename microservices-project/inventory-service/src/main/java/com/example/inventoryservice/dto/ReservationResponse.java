package com.example.inventoryservice.dto;

import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private String cartId;
    private String skuCode;
    private Integer quantity;
    private LocalDateTime expiresAt;

    public ReservationResponse() {
    }

    public ReservationResponse(Long id, String cartId, String skuCode, Integer quantity, LocalDateTime expiresAt) {
        this.id = id;
        this.cartId = cartId;
        this.skuCode = skuCode;
        this.quantity = quantity;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
