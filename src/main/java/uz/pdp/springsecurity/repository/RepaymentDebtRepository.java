package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.springsecurity.entity.RepaymentDebt;

import java.sql.Timestamp;
import java.util.UUID;

public interface RepaymentDebtRepository extends JpaRepository<RepaymentDebt, UUID> {
    @Query("select sum(r.debtSum) from RepaymentDebt r where r.customer.branch.id= :branchId and r.createdAt >= :startDate AND r.createdAt <= :endDate")
    Double getTotalSum(@Param("branchId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);
}