package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    boolean existsByName(String name);


    Optional<Role> findByName(String name);

    List<Role> findAllByBusiness_Id(UUID business_id);

    void deleteAllByBusiness_Id(UUID id);
}
