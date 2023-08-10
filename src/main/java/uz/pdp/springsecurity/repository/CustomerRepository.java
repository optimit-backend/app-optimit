package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Customer;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findAllByBusiness_IdAndActiveIsTrueOrActiveIsNull(UUID business_id);

    List<Customer> findAllByCustomerGroupIdAndActiveIsTrueOrActiveIsNull(UUID customerGroup_id);

    List<Customer> findAllByBusiness_IdAndBirthdayBetweenAndActiveIsTrueOrActiveIsNull(UUID business_id, Date start, Date end);

    List<Customer> findAllByBusiness_IdAndDebtIsNotOrderByPayDateAsc(UUID business_id, Double debt);

    List<Customer> findAllByBranchesIdAndActiveIsTrueOrActiveIsNull(UUID branchId);

    List<Customer> findAllByBranchesIdAndDebtIsNotOrderByPayDateAsc(UUID branchId, Double debt);

    List<Customer> findAllByBusinessIdAndDebtIsNotOrderByPayDateAsc(UUID businessId, Double debt);

    Optional<Customer> findByPhoneNumberAndActiveIsTrueOrActiveIsNull(String phoneNumber);

    List<Customer> findAllByPayDateBetweenAndBusinessIdAndActiveIsTrueOrActiveIsNull(Date payDate, Date payDate2, UUID business_id);

    List<Customer> findAllByBranchesIdAndLidCustomerIsTrueAndActiveIsTrueOrActiveIsNull(UUID branch_id);

    int countAllByBranchesId(UUID branchId);

    Integer countAllByBranch_IdAndCreatedAtBetween(UUID branch_id, Timestamp createdAt, Timestamp createdAt2);
    Integer countAllByBusiness_IdAndCreatedAtBetween(UUID businessId, Timestamp createdAt, Timestamp createdAt2);

    List<Customer> findAllByBranchIdAndNameContainingIgnoreCase(UUID branch_id, String name);
}
