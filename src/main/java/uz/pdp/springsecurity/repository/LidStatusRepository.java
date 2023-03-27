package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.LidStatus;

import java.util.List;
import java.util.UUID;

public interface LidStatusRepository extends JpaRepository<LidStatus, UUID> {
    List<LidStatus> findAllByBusiness_IdOrderBySortAsc(UUID business_id);

    List<LidStatus> findAllBySortGreaterThanEqual(Integer sort);
}
