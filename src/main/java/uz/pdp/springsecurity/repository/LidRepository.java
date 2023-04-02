package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Lid;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface LidRepository extends JpaRepository<Lid, UUID> {
    List<Lid> findAllByBusiness_Id(UUID business_id);

    Page<Lid> findAllByBusiness_IdAndLidStatusId(UUID business_id, UUID lidStatus_id, Pageable pageable);

    Page<Lid> findAllByBusinessId(UUID business_id, Pageable pageable);

    Page<Lid> findAllByBusinessIdAndCreatedAtBetween(UUID business_id, Timestamp startTime, Timestamp endTime, Pageable pageable);
}
