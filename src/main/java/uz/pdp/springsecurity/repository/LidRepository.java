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
    Page<Lid> findAllByLidStatus_Id(UUID lidStatus_id, Pageable pageable);
    List<Lid> findAllByLidStatusId(UUID lidStatus_id);
    List<Lid> findAllBySourceId(UUID source_id);
    List<Lid> findAllByLidStatus_OrginalName(String lidStatus_orginalName);
    int countByLidStatusId(UUID lidStatus_id);
    Page<Lid> findAllByLidStatusIdAndSourceId(UUID lidStatus_id, UUID source_id, Pageable pageable);

    Page<Lid> findAllByLidStatusIdAndCreatedAtBetween(UUID lidStatus_id, Timestamp startDate, Timestamp endDate, Pageable pageable);

    Page<Lid> findAllByLidStatusIdAndSourceIdAndCreatedAtBetween(UUID lidStatus_id, UUID source_id, Timestamp startDate, Timestamp endDate, Pageable pageable);

    Page<Lid> findAllByBusinessId(UUID business_id, Pageable pageable);

    Page<Lid> findAllByBusinessIdAndSourceId(UUID business_id, UUID source_id, Pageable pageable);

    Page<Lid> findAllByBusinessIdAndSourceIdAndCreatedAtBetween(UUID business_id, UUID source_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);

    Page<Lid> findAllByBusinessIdAndCreatedAtBetween(UUID business_id, Timestamp startTime, Timestamp endTime, Pageable pageable);
}
