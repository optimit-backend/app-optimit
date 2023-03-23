package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findAllByBusiness_Id(UUID business_id);
    List<Customer> findAllByBusiness_IdAndDebtIsNotOrderByPayDateAsc(UUID business_id, Double debt);

    List<Customer> findAllByBranchId(UUID branchId);
    List<Customer> findAllByBranchIdAndDebtIsNotOrderByPayDateAsc(UUID branchId, Double debt);

}
