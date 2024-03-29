package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.springsecurity.entity.RepaymentDebt;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface RepaymentDebtRepository extends JpaRepository<RepaymentDebt, UUID> {
    @Query("select sum(r.debtSum) from RepaymentDebt r where r.customer.branch.id= :branchId and r.paymentMethod.type = :paymentMethodName and r.createdAt >= :startDate AND r.createdAt <= :endDate and r.delete = false and r.payDate = null")
    Double getTotalSum(@Param("branchId") UUID branchId,@Param("paymentMethodName") String paymentMethodName, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query("select sum(r.debtSum) from RepaymentDebt r where r.customer.branch.id= :branchId and r.paymentMethod.type = :paymentMethodName and r.payDate >= :startDate AND r.payDate <= :endDate and r.delete = false ")
    Double getTotalSumWithPayDate(@Param("branchId") UUID branchId,@Param("paymentMethodName") String paymentMethodName, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query("select sum(r.debtSum) from RepaymentDebt r where r.customer.branch.business.id= :businessId and  r.paymentMethod.type = :paymentMethodName and r.createdAt >= :startDate AND r.createdAt <= :endDate and r.delete = false and r.payDate = null ")
    Double getTotalSumByBusiness(@Param("businessId") UUID branchId, @Param("paymentMethodName") String paymentMethodName, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query("select sum(r.debtSum) from RepaymentDebt r where r.customer.branch.business.id= :businessId and  r.paymentMethod.type = :paymentMethodName and r.payDate >= :startDate AND r.payDate <= :endDate and r.delete = false ")
    Double getTotalSumByBusinessWithPayDate(@Param("businessId") UUID branchId, @Param("paymentMethodName") String paymentMethodName, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    List<RepaymentDebt> findAllByCustomer_Id(UUID customer_id);
}