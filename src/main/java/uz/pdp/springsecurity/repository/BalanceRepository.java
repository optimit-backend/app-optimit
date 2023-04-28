package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Balance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BalanceRepository extends JpaRepository<Balance, UUID> {

    List<Balance> findAllByBranchId(UUID branchId);

    Optional<Balance> findByPaymentMethod_Id(UUID paymentMethod_id);
    Optional<Balance> findByPaymentMethod_TypeAndBranchId(String paymentMethod_type, UUID branch_id);
}
