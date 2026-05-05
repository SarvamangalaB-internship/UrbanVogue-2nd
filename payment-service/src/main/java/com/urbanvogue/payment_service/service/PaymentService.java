package com.urbanvogue.payment_service.service;

import com.urbanvogue.payment_service.dto.*;
import com.urbanvogue.payment_service.model.Payment;
import com.urbanvogue.payment_service.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Map.of;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.account.service.url}")
    private String userServiceUrl;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Value("${payment.simulation.success-rate:1.0}")
    private double successRate;


    //  INITIATE PAYMENT

    public PaymentResponse initiatePayment(PaymentRequest request) {

        String method = request.getPaymentMethod().toUpperCase();
        String txnId = "UV-TXN-" + UUID.randomUUID().toString().substring(0, 8);

        Payment p = new Payment();
        p.setOrderId(request.getOrderId());
        p.setCustomerUsername(request.getCustomerUsername());
        p.setAmount(request.getAmount());
        p.setPaymentMethod(method);
        p.setTransactionId(txnId);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        //  COD FLOW
        if (method.equals("COD")) {

            p.setStatus("COD_PENDING");
            repo.save(p);

            updateOrder(request.getOrderId(), "CONFIRMED");

            // Notification - COD
            try {
                restTemplate.postForObject(
                        "http://localhost:8085/api/notifications/payment-cod" +
                                "?username=" + p.getCustomerUsername() +
                                "&orderId=" + p.getOrderId() +
                                "&amount=" + p.getAmount(),
                        null,
                        String.class
                );
            } catch (Exception e) {
                System.out.println("Notification failed");
            }

            return new PaymentResponse(
                    p.getId(), p.getOrderId(), txnId,
                    "COD_PENDING", p.getAmount(), method,
                    "Pay on delivery"
            );
        }

        // UPI FLOW
        boolean success = Math.random() < successRate;

        if (success) {

            try {
                restTemplate.postForObject(
                        userServiceUrl + "/api/user/debit",
                        of(
                                "userId", p.getCustomerUsername(),
                                "amount", p.getAmount()
                        ),
                        String.class
                );
            } catch (Exception e) {

                p.setStatus("FAILED");
                p.setFailureReason("Debit failed");
                repo.save(p);

                //  Notification - FAILED
                try {
                    restTemplate.postForObject(
                            "http://localhost:8085/api/notifications/payment-failed" +
                                    "?username=" + p.getCustomerUsername() +
                                    "&orderId=" + p.getOrderId() +
                                    "&amount=" + p.getAmount(),
                            null,
                            String.class
                    );
                } catch (Exception ex) {
                    System.out.println("Notification failed");
                }

                return new PaymentResponse(
                        p.getId(), p.getOrderId(), txnId,
                        "FAILED", p.getAmount(), method,
                        "Insufficient balance"
                );
            }

            p.setStatus("SUCCESS");
            repo.save(p);

            updateOrder(request.getOrderId(), "CONFIRMED");

            // Notification - SUCCESS
            try {
                restTemplate.postForObject(
                        "http://localhost:8085/api/notifications/payment-success" +
                                "?username=" + p.getCustomerUsername() +
                                "&orderId=" + p.getOrderId() +
                                "&amount=" + p.getAmount() +
                                "&transactionId=" + txnId,
                        null,
                        String.class
                );
            } catch (Exception e) {
                System.out.println("Notification failed");
            }

            return new PaymentResponse(
                    p.getId(), p.getOrderId(), txnId,
                    "SUCCESS", p.getAmount(), method,
                    "Payment successful"
            );
        }

        //  FAILED (gateway failure)
        p.setStatus("FAILED");
        p.setFailureReason("Gateway error");
        repo.save(p);

        // Notification - FAILED
        try {
            restTemplate.postForObject(
                    "http://localhost:8085/api/notifications/payment-failed" +
                            "?username=" + p.getCustomerUsername() +
                            "&orderId=" + p.getOrderId() +
                            "&amount=" + p.getAmount(),
                    null,
                    String.class
            );
        } catch (Exception e) {
            System.out.println("Notification failed");
        }

        return new PaymentResponse(
                p.getId(), p.getOrderId(), txnId,
                "FAILED", p.getAmount(), method,
                "Payment failed"
        );
    }


    //  REFUND

    public PaymentResponse processRefund(String txnId) {

        Payment p = repo.findByTransactionId(txnId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!"SUCCESS".equals(p.getStatus())) {
            throw new RuntimeException("Refund not allowed");
        }

        try {
            restTemplate.postForObject(
                    userServiceUrl + "/api/user/credit",
                    Map.of(
                            "userId", p.getCustomerUsername(),
                            "amount", p.getAmount()
                    ),
                    String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Refund failed");
        }

        p.setStatus("REFUNDED");
        p.setUpdatedAt(LocalDateTime.now());
        repo.save(p);

        updateOrder(p.getOrderId(), "REFUNDED");

        //  Notification - REFUND
        try {
            restTemplate.postForObject(
                    "http://localhost:8085/api/notifications/refund" +
                            "?username=" + p.getCustomerUsername() +
                            "&orderId=" + p.getOrderId() +
                            "&amount=" + p.getAmount(),
                    null,
                    String.class
            );
        } catch (Exception e) {
            System.out.println("Notification failed");
        }

        return new PaymentResponse(
                p.getId(), p.getOrderId(), txnId,
                "REFUNDED", p.getAmount(), p.getPaymentMethod(),
                "Refund successful"
        );
    }

    //  COMPLETE COD

    public Payment completeCodPayment(String txnId) {

        Payment p = repo.findByTransactionId(txnId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (!p.getStatus().equals("COD_PENDING")) {
            throw new RuntimeException("Invalid COD state");
        }

        p.setStatus("SUCCESS");
        p.setUpdatedAt(LocalDateTime.now());

        return repo.save(p);
    }

    //  GET ALL

    public List<Payment> getAllPayments() {
        return repo.findAll();
    }

    //  UPDATE ORDER (FIXED)

    private void updateOrder(Long id, String status) {

        try {
            restTemplate.put(
                    orderServiceUrl +
                            "/api/orders/" + id +
                            "/status?status=" + status,
                    null
            );
        } catch (Exception e) {
            System.out.println("Order service down");
        }
    }
}