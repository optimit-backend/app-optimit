package uz.pdp.springsecurity.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.TradeProduct;

import java.util.List;
import java.util.UUID;

public interface TradeProductRepository extends JpaRepository<TradeProduct, UUID> {
List<TradeProduct> findAllByProduct_Id(UUID product_id);
List<TradeProduct> findAllByTradeId(UUID tradeId);
}
