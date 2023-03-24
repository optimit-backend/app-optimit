package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Agreement;
import uz.pdp.springsecurity.enums.SalaryStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgreementRepository extends JpaRepository<Agreement, UUID> {
    List<Agreement> findAllByUserId(UUID userId);

    Optional<Agreement> findByUserIdAndSalaryStatus(UUID userId, SalaryStatus salaryStatus);
    Agreement getByUserIdAndSalaryStatus(UUID userId, SalaryStatus salaryStatus);

    Integer countAllByUserId(UUID userId);
}
