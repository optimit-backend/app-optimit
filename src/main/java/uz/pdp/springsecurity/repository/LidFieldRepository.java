package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.LidField;

import java.util.List;
import java.util.UUID;

public interface LidFieldRepository extends JpaRepository<LidField, UUID> {
    List<LidField> findAllByBusiness_Id(UUID business_id);
    List<LidField> findAllByBusinessIsNull();
}
