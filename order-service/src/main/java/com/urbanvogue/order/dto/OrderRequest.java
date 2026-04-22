package com.urbanvogue.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {

    // Must match a registered user from Auth Service
    private String customerUsername;

    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {

        // Must be a valid product ID from Product Service
        private Long productId;

        // Copy the UPPERCASE name from Product Service
        // e.g. "URBAN CLASSIC SNEAKERS"
        private String productName;

        private Integer quantity;

        // Copy the price from Product Service GET response
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