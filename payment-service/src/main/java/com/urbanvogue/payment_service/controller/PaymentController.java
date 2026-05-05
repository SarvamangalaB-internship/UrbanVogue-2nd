package com.urbanvogue.payment_service.controller;

import com.urbanvogue.payment_service.dto.*;
import com.urbanvogue.payment_service.model.Payment;
import com.urbanvogue.payment_service.service.PaymentService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174","http://localhost:5175"})
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    // initiate payment
    @PostMapping("/initiate")
    public PaymentResponse initiate(@RequestBody PaymentRequest r) {
        return service.initiatePayment(r);
    }

    //  refund
    @PostMapping("/refund/{txn}")
    public PaymentResponse refund(@PathVariable String txn) {
        return service.processRefund(txn);
    }

    //  COD complete
    @PostMapping("/cod/complete/{txn}")
    public Payment completeCod(@PathVariable String txn) {
        return service.completeCodPayment(txn);
    }

    //  all payments
    @GetMapping
    public List<Payment> all() {
        return service.getAllPayments();
    }
}