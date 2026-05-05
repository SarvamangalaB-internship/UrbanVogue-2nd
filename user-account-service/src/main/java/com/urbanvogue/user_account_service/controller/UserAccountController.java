package com.urbanvogue.user_account_service.controller;

import com.urbanvogue.user_account_service.dto.*;
import com.urbanvogue.user_account_service.model.UserAccount;
import com.urbanvogue.user_account_service.service.UserAccountService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174","http://localhost:5175"})
public class UserAccountController {
    private final UserAccountService service;

    public UserAccountController(UserAccountService service) {
        this.service = service;
    }

    // Create account
    @PostMapping("/create")
    public UserAccount create(@RequestParam String username,
                              @RequestParam Double balance) {
        return service.createAccount(username, balance);
    }

    // Debit
    @PostMapping("/debit")
    public AccountResponse debit(@RequestBody DebitRequest req) {
        return service.debit(req);
    }

    //Credit
    @PostMapping("/credit")
    public AccountResponse credit(@RequestBody DebitRequest req) {
        return service.credit(req);
    }

    // Get account
    @GetMapping("/{username}")
    public UserAccount get(@PathVariable String username) {
        return service.getAccount(username);
    }
}