package com.urbanvogue.payment_service.repository;

import com.urbanvogue.payment_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByCustomerUsername(String username);

    List<Payment> findByStatus(String status);


}