package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.SalaryCount;

import java.util.List;
import java.util.UUID;

public interface SalaryCountRepository extends JpaRepository<SalaryCount, UUID> {
    List<SalaryCount> findAllByAgreement_UserIdOrderByDate(UUID userId);
}
