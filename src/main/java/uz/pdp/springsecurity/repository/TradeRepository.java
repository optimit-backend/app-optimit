package uz.pdp.springsecurity.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.springsecurity.entity.Trade;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID>, JpaSpecificationExecutor<Trade> {
    List<Trade> findAllByTrader_Id(UUID trader_id);


    Page<Trade> findAllByBranch_Business_IdAndLidIsTrue(UUID branch_business_id, Pageable pageable);

    List<Trade> findAllByBranch_IdOrderByCreatedAtDesc(UUID branch_id);

    List<Trade> findAllByCreatedAtBetweenAndBranchId(Timestamp start, Timestamp end, UUID branch_id);

    List<Trade> findAllByCreatedAtBetweenAndBranch_BusinessId(Timestamp start, Timestamp end, UUID businessId);

    List<Trade> findAllByBranch_Business_IdOrderByCreatedAtDesc(UUID businessId);

    double countAllByBranch_BusinessIdAndCreatedAtBetween(UUID branch_business_id, Timestamp createdAt, Timestamp createdAt2);

    double countAllByBranchIdAndCreatedAtBetween(UUID branch_id, Timestamp createdAt, Timestamp createdAt2);

    @Query(value = "select sum (t.totalSum) from Trade t where t.branch.id = :branchId AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Double totalSum(@Param("branchId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "select sum (t.totalSum) from Trade t where t.payMethod.id = :payMethodId and t.branch.id = :branchId AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Double totalPayment(@Param("payMethodId") UUID payMethodId, @Param("branchId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "select sum (t.totalSum) from Trade t where t.payMethod.id = :payMethodId and t.branch.business.id = :businessId AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Double totalPaymentByBusiness(@Param("payMethodId") UUID payMethodId, @Param("businessId") UUID businessId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "select sum (t.totalSum) from Trade t where t.branch.business.id = :businessId AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Double totalSumByBusiness(@Param("businessId") UUID businessId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "select sum (t.totalProfit) from Trade t where t.branch.id = :branchId AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Double totalProfit(@Param("branchId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "select sum (t.totalProfit) from Trade t where t.branch.business.id = :businessId AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Double totalProfitByBusinessId(@Param("businessId") UUID businessId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "SELECT SUM(t.debtSum) FROM Trade t WHERE t.branch.id = :branchId AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Double totalDebtSum(@Param("branchId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "SELECT SUM(t.debtSum) FROM Trade t WHERE t.branch.business.id = :businessId AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Double totalDebtSumByBusiness(@Param("businessId") UUID businessId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);


    List<Trade> findAllByCustomer_Id(UUID customer_id);

    List<Trade> findAllByCustomerIdAndDebtSumIsNotOrderByCreatedAtAsc(UUID customerId, Double amount);

    /*List<Trade> findAllByPaymentStatus_Id(UUID paymentStatus_id);

    List<Trade> findAllByAddress_Id(UUID address_id);

    @Query(value = "SELECT * FROM Trade t WHERE DATE(t.pay_date) = ?1", nativeQuery = true)
    List<Trade> findTradeByOneDate(Timestamp date);*/

    List<Trade> findAllByCreatedAtBetweenAndCustomer_Id(Timestamp startTimestamp, Timestamp endTimestamp, UUID customer_id);

    String getTraderNameById(UUID traderId);

    @Query(value = "SELECT SUM(total_sum) FROM trade WHERE created_at BETWEEN ?1 AND ?2 AND branch_id = ?3", nativeQuery = true)
    Double totalSumByCreatedAtBetweenAndBranchId(Timestamp from, Timestamp to, UUID branch_id);

    Optional<Trade> findFirstByBranchIdOrderByCreatedAtDesc(UUID branchId);

    Page<Trade> findAllByBranchIdOrderByCreatedAtDesc(UUID branchId, Pageable pageable);

    Page<Trade> findAllByBranchIdAndInvoiceContainingOrderByCreatedAtDesc(UUID branchId, String invoice, Pageable pageable);

    Page<Trade> findAllByBranch_BusinessIdOrderByCreatedAtDesc(UUID businessId, Pageable pageable);

    Page<Trade> findAllByBranch_BusinessIdAndInvoiceContainingOrderByCreatedAtDesc(UUID businessId, String invoice, Pageable pageable);


}