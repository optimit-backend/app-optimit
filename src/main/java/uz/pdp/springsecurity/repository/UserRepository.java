package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Branch;
import uz.pdp.springsecurity.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    boolean existsByUsernameAndIdNot(String userName, UUID id);

    List<User> findAllByRole_Id(UUID role_id);

    List<User> findAllByBusiness_Id(UUID business_id);

//    List<User> findAllByBranches(Set<Branch> branches);

    List<User> findAllByBranchesId(UUID branches_id);

}