package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.WorkTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkTimeRepository extends JpaRepository<WorkTime, UUID> {
    boolean existsByUserIdAndBranchIdAndActiveTrue(UUID userId, UUID branchId);

    Optional<WorkTime> findByUserIdAndBranchIdAndActiveTrue(UUID userID, UUID branchId);

    List<WorkTime> findAllByUserIdAndBranchId(UUID userId, UUID branchId);
    List<WorkTime> findAllByBranchIdAndActiveTrue(UUID branchId);
}
