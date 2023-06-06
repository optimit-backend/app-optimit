package uz.pdp.springsecurity.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.springsecurity.entity.Trade;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findAllByTrader_Id(UUID trader_id);


    Page<Trade> findAllByBranch_Business_IdAndLidIsTrue(UUID branch_business_id, Pageable pageable);

    List<Trade> findAllByBranch_IdOrderByCreatedAtDesc(UUID branch_id);

    List<Trade> findAllByCreatedAtBetweenAndBranchId(Timestamp start, Timestamp end, UUID branch_id);
    List<Trade> findAllByCreatedAtBetweenAndBranch_BusinessId(Timestamp start, Timestamp end, UUID businessId);

    List<Trade> findAllByBranch_BusinessIdOrderByCreatedAtDesc(UUID businessId);

    List<Trade> findAllByCustomer_Id(UUID customer_id);
    List<Trade> findAllByCustomerIdAndDebtSumIsNotOrderByCreatedAtAsc(UUID customerId, Double amount);

    List<Trade> findAllByPaymentStatus_Id(UUID paymentStatus_id);

    List<Trade> findAllByAddress_Id(UUID address_id);

    @Query(value = "SELECT * FROM Trade t WHERE DATE(t.pay_date) = ?1", nativeQuery = true)
    List<Trade> findTradeByOneDate(Timestamp date);

    List<Trade> findAllByCreatedAtBetweenAndCustomer_Id(Timestamp startTimestamp, Timestamp endTimestamp, UUID customer_id);

    String getTraderNameById(UUID traderId);

    @Query(value = "SELECT SUM(total_sum) FROM trade WHERE created_at BETWEEN ?1 AND ?2 AND branch_id = ?3", nativeQuery = true)
    Double totalSumByCreatedAtBetweenAndBranchId(Timestamp from, Timestamp to, UUID branch_id);
}