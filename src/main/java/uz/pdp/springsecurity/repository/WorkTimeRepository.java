package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.WorkTime;

import java.util.UUID;

public interface WorkTimeRepository extends JpaRepository<WorkTime, UUID> {
    boolean existsByUserIdAndActiveTrue(UUID userId);
}
