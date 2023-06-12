package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.PaymentMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    boolean existsByType(String type);

    List<PaymentMethod> findAllByBusiness_Id(UUID business_id);

    Optional<PaymentMethod> findByTypeAndBusiness_id(String type, UUID business_id);
}
