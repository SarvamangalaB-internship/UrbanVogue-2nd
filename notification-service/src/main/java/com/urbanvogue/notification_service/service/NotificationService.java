package com.urbanvogue.notification_service.service;

import com.urbanvogue.notification_service.dto.NotificationRequest;
import com.urbanvogue.notification_service.model.Notification;
import com.urbanvogue.notification_service.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    // MAIN METHOD used
    public Notification sendNotification(NotificationRequest req) {

        if (req.getRecipientUsername() == null || req.getRecipientUsername().isEmpty()) {
            throw new RuntimeException("Username required");
        }

        Notification n = new Notification();

        n.setRecipientUsername(req.getRecipientUsername());
        n.setType(req.getType() != null ? req.getType().toUpperCase() : "GENERAL");
        n.setMessage(req.getMessage());
        n.setChannel(req.getChannel() != null ? req.getChannel().toUpperCase() : "EMAIL");
        n.setReferenceId(req.getReferenceId());
        n.setStatus("PENDING");
        n.setCreatedAt(LocalDateTime.now());

        Notification saved = repo.save(n);

        // simulate sending
        saved.setStatus("SENT");
        saved.setSentAt(LocalDateTime.now());

        print(saved);

        return repo.save(saved);
    }



    public Notification orderPlaced(String user, Long orderId, Double amount) {
        return sendNotification(build(user, "ORDER_PLACED",
                "Order #" + orderId + " placed. Amount Rs." + amount, "EMAIL", orderId));
    }

    public Notification paymentSuccess(String user, Long orderId, Double amount, String txn) {
        return sendNotification(build(user, "PAYMENT_SUCCESS",
                "Payment Rs." + amount + " success. Txn: " + txn, "SMS", orderId));
    }

    public Notification paymentFailed(String user, Long orderId, Double amount) {
        return sendNotification(build(user, "PAYMENT_FAILED",
                "Payment failed Rs." + amount, "SMS", orderId));
    }

    public Notification orderCancelled(String user, Long orderId) {
        return sendNotification(build(user, "ORDER_CANCELLED",
                "Order #" + orderId + " cancelled", "EMAIL", orderId));
    }

    public Notification refund(String user, Long orderId, Double amount) {
        return sendNotification(build(user, "REFUND",
                "Refund Rs." + amount + " initiated", "SMS", orderId));
    }

    public Notification welcome(String user) {
        return sendNotification(build(user, "WELCOME",
                "Welcome " + user + " ", "EMAIL", null));
    }


    private NotificationRequest build(String u, String t, String m, String c, Long ref) {
        NotificationRequest r = new NotificationRequest();
        r.setRecipientUsername(u);
        r.setType(t);
        r.setMessage(m);
        r.setChannel(c);
        r.setReferenceId(ref);
        return r;
    }

    //  GET APIs
    public List<Notification> getAll() {
        return repo.findAll();
    }

    public List<Notification> getUser(String username) {
        return repo.findByRecipientUsername(username);
    }

    public Notification getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }

    public List<Notification> getByType(String type) {
        return repo.findByType(type.toUpperCase());
    }

    public List<Notification> getByStatus(String status) {
        return repo.findByStatus(status.toUpperCase());
    }

    private void print(Notification n) {
        System.out.println(" " + n.getMessage());
    }
}