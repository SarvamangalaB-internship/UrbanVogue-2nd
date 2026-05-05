package com.urbanvogue.order_service.repository;

import com.urbanvogue.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerUsername(String customerUsername);

    List<Order> findByStatus(String status);


}