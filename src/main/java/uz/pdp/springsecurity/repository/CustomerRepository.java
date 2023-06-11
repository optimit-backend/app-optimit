package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Customer;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findAllByBusiness_Id(UUID business_id);

    List<Customer> findAllByCustomerGroupId(UUID customerGroup_id);

    List<Customer> findAllByBusiness_IdAndBirthdayBetween(UUID business_id, Date start, Date end);

    List<Customer> findAllByBusiness_IdAndDebtIsNotOrderByPayDateAsc(UUID business_id, Double debt);

    List<Customer> findAllByBranchesId(UUID branchId);

    List<Customer> findAllByBranchesIdAndDebtIsNotOrderByPayDateAsc(UUID branchId, Double debt);

    List<Customer> findAllByBusinessIdAndDebtIsNotOrderByPayDateAsc(UUID businessId, Double debt);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    List<Customer> findAllByPayDateBetweenAndBusinessId(Date payDate, Date payDate2, UUID business_id);

    List<Customer> findAllByBranchesIdAndLidCustomerIsTrue(UUID branch_id);

    int countAllByBranchesId(UUID branchId);

    Integer countAllByBranch_IdAndCreatedAtBetween(UUID branch_id, Timestamp createdAt, Timestamp createdAt2);
    Integer countAllByBusiness_IdAndCreatedAtBetween(UUID businessId, Timestamp createdAt, Timestamp createdAt2);
}
