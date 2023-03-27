package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Lid;

import java.util.List;
import java.util.UUID;

public interface LidRepository extends JpaRepository<Lid, UUID> {

    List<Lid> findAllByLidStatus_BusinessId(UUID lidStatus_business_id);

    List<Lid> findAllByLidStatus_BusinessIdOrderByLidStatus_Sort(UUID businessId);
}
