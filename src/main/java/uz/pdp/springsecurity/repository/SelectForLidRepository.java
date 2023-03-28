package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.SelectForLid;

import java.util.List;
import java.util.UUID;

public interface SelectForLidRepository extends JpaRepository<SelectForLid, UUID> {
    List<SelectForLid> findAllByLid_BusinessId(UUID lid_business_id);
}