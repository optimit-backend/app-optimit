package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.FormLidHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormLidHistoryRepository extends JpaRepository<FormLidHistory, UUID> {
    Optional<FormLidHistory> findByActiveIsTrue();

    List<FormLidHistory> findAllByBusinessIdOrderByCreatedAtAsc(UUID business_id);
}
