package com.example.orderservice.dto;

public class InventoryDeductionRequest {

    private String skuCode;
    private Integer quantity;

    public InventoryDeductionRequest() {
    }

    public InventoryDeductionRequest(String skuCode, Integer quantity) {
        this.skuCode = skuCode;
        this.quantity = quantity;
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
}
