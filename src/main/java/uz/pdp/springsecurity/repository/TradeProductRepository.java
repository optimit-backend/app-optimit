package uz.pdp.springsecurity.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.ProductTypePrice;
import uz.pdp.springsecurity.entity.TradeProduct;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeProductRepository extends JpaRepository<TradeProduct, UUID> {
  List<TradeProduct> findAllByProduct_Id(UUID product_id);

  List<TradeProduct> findAllByTrade_BranchIdAndProduct_Id(UUID trade_branch_id, UUID product_id);

  List<TradeProduct> findAllByCreatedAtBetweenAndProduct_BranchId(Timestamp createdAt, Timestamp createdAt2, UUID product_branch_id);

  List<TradeProduct> findAllByProduct_IdAndTrade_CustomerId(UUID product_id, UUID trade_customer_id);

  List<TradeProduct> findAllByCreatedAtBetweenAndProductId(Timestamp startDate, Timestamp endDate, UUID product_id);
  List<TradeProduct> findAllByCreatedAtBetweenAndProductTypePriceId(Timestamp startDate, Timestamp endDate, UUID product_id);
  List<TradeProduct> findAllByCreatedAtBetween(Timestamp createdAt, Timestamp createdAt2);
  List<TradeProduct> findAllByCreatedAtBetweenAndProduct_CategoryId(Timestamp startDate, Timestamp endDate, UUID product_category_id);
  List<TradeProduct> findAllByCreatedAtBetweenAndProductTypePrice_Product_CategoryId(Timestamp startDate, Timestamp endDate, UUID product_category_id);
  List<TradeProduct> findAllByCreatedAtBetweenAndProduct_BrandId(Timestamp startDate, Timestamp endDate, UUID product_brand_id);
    List<TradeProduct> findAllByCreatedAtBetweenAndProductTypePrice_Product_BrandId(Timestamp startDate, Timestamp endDate, UUID product_brand_id);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_CustomerId(Timestamp startDate, Timestamp endDate, UUID trade_customer_id);
  List<TradeProduct> findAllByProduct_BrandId(UUID brandId);
  List<TradeProduct> findAllByProductTypePrice_Product_BrandId(UUID brandId);
  List<TradeProduct> findAllByTradeId(UUID tradeId);
  List<TradeProduct> findAllByProduct_BranchId(UUID product_branch_id);
  List<TradeProduct> findAllByTrade_BranchId(UUID product_branch_id);
  List<TradeProduct> findAllByTrade_BranchIdAndProductTypePriceId(UUID trade_branch_id, UUID productTypePrice_id);
  List<TradeProduct> findAllByProduct_CategoryIdAndTrade_BranchId(UUID categoryId, UUID branchId);
  List<TradeProduct> findAllByProductTypePrice_Product_CategoryIdAndTrade_BranchId(UUID productTypePrice_product_category_id, UUID trade_branch_id);
  List<TradeProduct> findAllByProduct_BrandIdAndTrade_BranchId(UUID brandId, UUID branchId);
  List<TradeProduct> findAllByProductTypePrice_Product_BrandIdAndTrade_BranchId(UUID brandId, UUID branchId);
  List<TradeProduct> findAllByTrade_PayMethodIdAndTrade_BranchId(UUID payMethodId, UUID branchId);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_PayMethodIdAndTrade_BranchId(Timestamp from, Timestamp to, UUID payMethodId, UUID branchId);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_CustomerIdAndTrade_BranchId(Timestamp from, Timestamp to, UUID customerId, UUID branchId);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_BranchId(Timestamp from, Timestamp to, UUID branchId);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_BranchIdAndProduct_CategoryId(Timestamp from, Timestamp to,UUID branchId,UUID categoryId);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_BranchIdAndProductTypePrice_Product_CategoryId(Timestamp from, Timestamp to,UUID branchId,UUID categoryId);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_BranchIdAndProduct_BrandId(Timestamp from, Timestamp to,UUID branchId,UUID brandId);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_BranchIdAndProductTypePrice_Product_BrandId(Timestamp from, Timestamp to,UUID branchId,UUID brandId);
  List<TradeProduct> findAllByProduct_CategoryIdAndProduct_BrandIdAndTrade_BranchId(UUID categoryId, UUID brandId, UUID branchId);
  List<TradeProduct> findAllByProductTypePrice_Product_CategoryIdAndProductTypePrice_Product_BrandIdAndTrade_BranchId(UUID categoryId, UUID brandId, UUID branchId);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_BranchIdAndProduct_CategoryIdAndProduct_BrandId(Timestamp from, Timestamp to, UUID branchId, UUID categoryId, UUID brandId);
  List<TradeProduct> findAllByCreatedAtBetweenAndTrade_BranchIdAndProductTypePrice_Product_CategoryIdAndProductTypePrice_Product_BrandId(Timestamp from, Timestamp to, UUID branchId, UUID categoryId, UUID brandId);
  List<TradeProduct> findAllByTrade_CustomerIdAndTrade_BranchId(UUID customerId, UUID branchId);
  List<TradeProduct> findAllByTrade_CustomerIdAndTrade_BranchIdAndTrade_PayMethodId(UUID customerId, UUID branchId, UUID payMethodId);
  List<TradeProduct> findAllByTrade_CustomerId(UUID customerId);
  List<TradeProduct> findAllByCreatedAtBetweenAndProduct_BranchIdAndTrade_CustomerId(Timestamp from, Timestamp to, UUID branchId, UUID customerId);
  List<TradeProduct> findAllByProduct_Business_IdOrderByTradedQuantity(UUID product_business_id);

    TradeProduct findByProduct_Id(UUID key);

    List<TradeProduct> findAllByCreatedAtBetweenAndTrade_Customer_Id(Timestamp createdAt, Timestamp createdAt2, UUID trade_customer_id);
    List<TradeProduct> findAllByCreatedAtBetweenAndProduct_BusinessId(Timestamp createdAt, Timestamp createdAt2, UUID product_business_id);

    List<TradeProduct> findAllByProductTypePriceId(UUID productTypePrice_id);
    List<TradeProduct> findAllByProductTypePrice(ProductTypePrice productTypePrice);

    List<TradeProduct> findAllByProductTypePriceIdAndTrade_CustomerId(UUID productTypePrice_id, UUID trade_customer_id);

  List<TradeProduct> findAllByProduct_CategoryId(UUID id);
  List<TradeProduct> findAllByProductTypePrice_Product_CategoryId(UUID id);

  Optional<TradeProduct> findByProductTypePriceId(UUID id);
}

