package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.WorkTime;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkTimeRepository extends JpaRepository<WorkTime, UUID> {
    boolean existsByUserIdAndBranchIdAndActiveTrue(UUID userId, UUID branchId);

    Optional<WorkTime> findByUserIdAndBranchIdAndActiveTrue(UUID userID, UUID branchId);

    List<WorkTime> findAllByUserIdAndBranchId(UUID userId, UUID branchId);
    Optional<WorkTime> findByUserIdAndBranchIdAndArrivalTimeIsBetween(UUID user_id, UUID branch_id, Timestamp from, Timestamp to);
    Optional<WorkTime> findFirstByUserIdAndBranchIdAndArrivalTimeIsBetween(UUID user_id, UUID branch_id, Timestamp from, Timestamp to);
    boolean existsByUserIdAndBranchIdAndArrivalTimeIsBetween(UUID user_id, UUID branch_id, Timestamp from, Timestamp to);

    void deleteAllByUserIdAndBranchIdAndActiveFalse(UUID userId, UUID branchId);
}
