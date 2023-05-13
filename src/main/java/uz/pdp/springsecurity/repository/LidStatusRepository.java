package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Lid;
import uz.pdp.springsecurity.entity.LidStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LidStatusRepository extends JpaRepository<LidStatus, UUID> {
    List<LidStatus> findAllByBusiness_IdOrderBySortAsc(UUID business_id);

    List<LidStatus> findAllByBusinessIsNullOrderBySortAsc();

    boolean existsBySaleStatusIsTrue();
    List<LidStatus> findAllBySortGreaterThanEqual(Integer sort);

    List<LidStatus> findAllByOrderBySortAsc();

    Optional<LidStatus> findByName(String name);
    Optional<LidStatus> findBySortAndBusinessId(Integer sort, UUID business_id);
}
