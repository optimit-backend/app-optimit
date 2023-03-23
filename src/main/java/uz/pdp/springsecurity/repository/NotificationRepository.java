package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByReadIsFalseAndUserToId(UUID userTo_id);
    List<Notification> findAllByReadIsTrueAndUserToId(UUID userTo_id);
}
