package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.springsecurity.entity.Purchase;


import java.sql.Timestamp;
import java.util.List;
import java.util.*;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
    List<Purchase> findAllByPurchaseStatus_Id(UUID purchaseStatus_id);

    List<Purchase> findAllByPaymentStatus_Id(UUID paymentStatus_id);

    List<Purchase> findAllByBranch_Id(UUID branch_id);

    @Query("SELECT s.name, s.phoneNumber, SUM(p.totalSum) FROM Purchase p JOIN p.supplier s WHERE p.branch.id = :branchId GROUP BY s.name, s.phoneNumber ORDER BY SUM(p.totalSum) DESC")
    List<Object[]> findTop10SuppliersByPurchase(@Param("branchId") UUID branchId);

    @Query("SELECT s.name, s.phoneNumber, SUM(p.totalSum) FROM Purchase p JOIN p.supplier s WHERE p.branch.id = :branchId AND p.date >= :startDate AND p.date <= :endDate GROUP BY s.name, s.phoneNumber ORDER BY SUM(p.totalSum) DESC")
    List<Object[]> findTop10SuppliersByPurchaseAndDate(@Param("branchId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    List<Purchase> findAllByCreatedAtBetweenAndBranchId(Timestamp start, Timestamp end, UUID branch_id);

    List<Purchase> findAllByCreatedAtBetweenAndBranch_BusinessId(Timestamp start, Timestamp end, UUID businessId);

    List<Purchase> findAllBySupplierId(UUID dealer_id);

    List<Purchase> findAllByBranch_BusinessId(UUID businessId);

    @Query(value = "select sum (p.totalSum) from Purchase p where p.branch.id = :branchId and p.date >= :startDate and p.date <= :endDate")
    Double totalPurchase(@Param("branchId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

//    @Query("select sum (p.totalSum) from Purchase p where p.branch.business.id = : businessId and p.date >= :startDate and p.date <= :endDate")
//    Double totalPurchaseByBusiness(@Param("businessId") UUID businessId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "select sum (p.debtSum) from Purchase p where p.branch.id = :branchId and p.date >= :startDate and p.date <= :endDate")
    Double totalMyDebt(@Param("branchId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

//    @Query("select sum (p.debtSum) from Purchase p where p.branch.business.id = : businessId and p.date >= :startDate and p.date <= :endDate")
//    Double totalMyDebtByBusiness(@Param("businessId") UUID businessId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);
}
