package com.urbanvogue.user_account_service.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {

    private String message;
    private Double balance;

    public AccountResponse(String amountDeducted, Double balance) {
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Double getBalance() {
        return balance;
    }
    public void setBalance(Double balance) {
        this.balance = balance;
    }




}