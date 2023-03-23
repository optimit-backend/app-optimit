package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Business;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Business> findByName(String name);

    List<Business> findAllByDeleteIsFalse();

    Integer countAllByCreatedAtAfter(Timestamp startTime);
}
