package com.urbanvogue.user_account_service.dto;

import lombok.Data;

@Data
public class DebitRequest {

    private String userId;   // IMPORTANT: match payment-service
    private Double amount;
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }


}