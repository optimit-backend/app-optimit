package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Prize;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface PrizeRepository extends JpaRepository<Prize, UUID> {
    List<Prize> findAllByBranchId(UUID branchId);
    List<Prize> findAllByBranchIdAndUserSetIdOrderByDateDesc(UUID branchId, UUID userId);
    List<Prize> findAllByBranchIdAndUserSetIdAndDateAfterAndGivenTrue(UUID branchId, UUID userId, Date date);
}
