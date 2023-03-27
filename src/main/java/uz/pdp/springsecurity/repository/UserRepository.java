package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Role;
import uz.pdp.springsecurity.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsById(UUID userId);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<User> findByUsername(String username);
    List<User> findAllByRole_Id(UUID role_id);


    List<User> findAllByRole_IdAndBusiness_Delete(UUID role_id, boolean delete);

    List<User> findAllByBusiness_Id(UUID business_id);
    List<User> findAllByBusiness_IdAndRoleIsNotAndActiveIsTrue(UUID business_id, Role role);
    List<User> findAllByBranchesIdAndRoleIsNotAndActiveIsTrue(UUID branches_id, Role role);
    Optional<User> findByBusinessIdAndRoleName(UUID business_id, String role_name);
}