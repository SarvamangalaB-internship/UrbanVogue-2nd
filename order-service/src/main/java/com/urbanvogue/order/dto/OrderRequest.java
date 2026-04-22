package com.urbanvogue.order.dto;

import lombok.Data;
import java.util.List;

@Data  // ← Generates getters/setters for outer class
public class OrderRequest {

    private String customerUsername;
    private List<OrderItemRequest> items;

    @Data  // ← CRITICAL: Must also be here for inner class!
    public static class OrderItemRequest {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double priceAtPurchase;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}