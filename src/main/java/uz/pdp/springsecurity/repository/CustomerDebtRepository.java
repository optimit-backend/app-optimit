package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.springsecurity.entity.CustomerDebt;

import java.sql.Timestamp;
import java.util.UUID;

public interface CustomerDebtRepository extends JpaRepository<CustomerDebt, UUID> {

    @Query(value = "select sum (c.debtSum) from CustomerDebt c where c.customer.business.id = :businessId AND c.createdAt >= :startDate AND c.createdAt <= :endDate")
    Double totalCustomerDebtSumByBusiness(@Param("businessId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "select sum (c.debtSum) from CustomerDebt c where c.customer.branch.id = :branchId AND c.createdAt >= :startDate AND c.createdAt <= :endDate")
    Double totalCustomerDebtSum(@Param("branchId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);
}
