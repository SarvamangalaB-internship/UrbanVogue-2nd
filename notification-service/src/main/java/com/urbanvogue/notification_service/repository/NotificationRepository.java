package com.urbanvogue.notification_service.repository;

import com.urbanvogue.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUsername(String username);

    List<Notification> findByType(String type);

    List<Notification> findByStatus(String status);
}