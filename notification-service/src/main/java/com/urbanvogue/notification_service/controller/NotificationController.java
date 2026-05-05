package com.urbanvogue.notification_service.controller;

import com.urbanvogue.notification_service.dto.NotificationRequest;
import com.urbanvogue.notification_service.model.Notification;
import com.urbanvogue.notification_service.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174","http://localhost:5175"})

public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/send")
    public Notification send(@RequestBody NotificationRequest req) {
        return service.sendNotification(req);
    }

    @PostMapping("/order-placed")
    public Notification orderPlaced(String username, Long orderId, Double amount) {
        return service.orderPlaced(username, orderId, amount);
    }

    @PostMapping("/payment-success")
    public Notification paymentSuccess(String username, Long orderId,
                                       Double amount, String txn) {
        return service.paymentSuccess(username, orderId, amount, txn);
    }

    @PostMapping("/payment-failed")
    public Notification paymentFailed(String username, Long orderId, Double amount) {
        return service.paymentFailed(username, orderId, amount);
    }

    @PostMapping("/order-cancelled")
    public Notification cancel(String username, Long orderId) {
        return service.orderCancelled(username, orderId);
    }

    @PostMapping("/refund")
    public Notification refund(String username, Long orderId, Double amount) {
        return service.refund(username, orderId, amount);
    }

    @PostMapping("/welcome")
    public Notification welcome(String username) {
        return service.welcome(username);
    }

    @GetMapping
    public List<Notification> all() {
        return service.getAll();
    }

    @GetMapping("/user/{username}")
    public List<Notification> user(@PathVariable String username) {
        return service.getUser(username);
    }

    @GetMapping("/{id}")
    public Notification one(@PathVariable Long id) {
        return service.getById(id);
    }
}