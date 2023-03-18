package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.TradeHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, UUID> {
    List<TradeHistory> findAllByTrade_Id(UUID trade_id);

    void deleteAllByTrade_Id(UUID trade_id);

    void deleteByTrade_Id(UUID trade_id);

    Optional<TradeHistory> findByIdAndTrade_Id(UUID id, UUID trade_id);

    Optional<TradeHistory> findByTrade_Id(UUID trade_id);

    List<TradeHistory> findAllByTrade_Branch_Id(UUID branch_id);

    List<TradeHistory> findAllByTrade_Branch_Business_Id(UUID business_id);
}
