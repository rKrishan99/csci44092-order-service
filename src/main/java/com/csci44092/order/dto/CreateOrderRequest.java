package com.csci44092.order.dto;

public class CreateOrderRequest {

    private String customerId;
    private Long productId;
    private Integer quantity;

    public CreateOrderRequest() {}

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}