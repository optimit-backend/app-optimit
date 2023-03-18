package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Business;

import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    boolean existsByName(String name);
}
