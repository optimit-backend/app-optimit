package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Salary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaryRepository extends JpaRepository<Salary, UUID> {
    Optional<Salary> findByUserIdAndBranch_IdAndActiveTrue(UUID userId, UUID branchId);
    Optional<Salary> findByIdAndActiveTrue(UUID salaryId);
    List<Salary> findAllByBranchIdAndActiveTrue(UUID branchId);
    List<Salary> findAllByUserIdAndBranchId(UUID userId, UUID branchId);
}
