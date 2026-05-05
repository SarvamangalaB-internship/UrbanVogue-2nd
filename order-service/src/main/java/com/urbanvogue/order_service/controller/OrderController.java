package com.urbanvogue.order_service.controller;

import com.urbanvogue.order_service.dto.OrderRequest;
import com.urbanvogue.order_service.model.Order;
import com.urbanvogue.order_service.service.OrderService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174","http://localhost:5175"})
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public Order placeOrder(@RequestBody OrderRequest request) {
        return service.placeOrder(request);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return service.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return service.getOrderById(id);
    }

    @GetMapping("/history/{username}")
    public List<Order> orderHistory(@PathVariable String username) {
        return service.getOrdersByCustomer(username);
    }

    @PutMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id,
                              @RequestParam String status) {
        return service.updateOrderStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public String cancel(@PathVariable Long id) {
        return service.cancelOrder(id);
    }
}