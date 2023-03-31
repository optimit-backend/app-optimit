package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Lid;

import java.util.List;
import java.util.UUID;

public interface LidRepository extends JpaRepository<Lid, UUID> {

    List<Lid> findAllByLidStatus_BusinessId(UUID lidStatus_business_id);

    List<Lid> findAllByBusinessId(UUID business_id);

    List<Lid> findAllByLidStatus_BusinessIdOrderByLidStatus_Sort(UUID businessId);

    Page<Lid> findAllByBusiness_IdAndLidStatusId(UUID business_id, UUID lidStatus_id, Pageable pageable);
}
