package uz.pdp.springsecurity.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.springsecurity.entity.Trade;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findAllByTrader_Id(UUID trader_id);

    List<Trade> findAllByBranch_Id(UUID branch_id);

    List<Trade> findAllByCustomer_Id(UUID customer_id);

    List<Trade> findAllByPaymentStatus_Id(UUID paymentStatus_id);

    List<Trade> findAllByPayMethod_Id(UUID payMethod_id);

    List<Trade> findAllByAddress_Id(UUID address_id);

    void deleteByTrader_Id(UUID trader_id);

    boolean existsByTraderId(UUID traderId);

    void deleteAllByTrader_Id(UUID trader_id);

    List<Trade> findAllByPayDateIsBetweenAndBranch_Id(Date payDate, Date payDate2, UUID branch_id);


    @Query(value = "SELECT * FROM Trade t WHERE DATE(t.pay_date) = ?1", nativeQuery = true)
    List<Trade> findTradeByOneDate(Timestamp date);

    @Query(value = "select * from Trade t inner join branches b on t.branch_id = b.id where b.business_id = ?1",nativeQuery = true)
    List<Trade> findAllByBusinessId(UUID businessId);

    List<Trade> findAllByPayDateAndBranchBetween(Date payDate,Date date2, UUID id);

}