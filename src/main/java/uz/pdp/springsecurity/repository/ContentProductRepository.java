package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.springsecurity.entity.ContentProduct;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface ContentProductRepository extends JpaRepository<ContentProduct, UUID> {
    List<ContentProduct> findAllByContentId(UUID contentId);
    List<ContentProduct> findAllByProductionId(UUID productionId);
    Page<ContentProduct> findAllByProduction_BranchIdAndProductIdAndProductionIsNotNullAndByProductIsFalseAndLossProductIsFalseOrderByCreatedAtDesc(UUID branchId, UUID productID, Pageable pageable);
    Page<ContentProduct> findAllByProduction_BranchIdAndProductTypePriceIdAndProductionIsNotNullAndByProductIsFalseAndLossProductIsFalseOrderByCreatedAtDesc(UUID branchId, UUID productTypePriceID, Pageable pageable);

    void deleteAllByContentId(UUID contentId);

    @Query(value = "SELECT SUM(quantity) FROM content_product WHERE loss_product = 'false' AND by_product = 'false' AND created_at BETWEEN ?1 AND ?2 AND product_id = ?3 AND production_id IN (SELECT id FROM production WHERE branch_id = ?4)", nativeQuery = true)
    Double quantityByBranchIdAndProductIdAndCreatedAtBetween(Timestamp from, Timestamp to, UUID productId, UUID branchId);

    @Query(value = "SELECT SUM(quantity) FROM content_product WHERE loss_product = 'false' AND by_product = 'false' AND created_at BETWEEN ?1 AND ?2 AND product_type_price_id = ?3 AND production_id IN (SELECT id FROM production WHERE branch_id = ?4)", nativeQuery = true)
    Double quantityByBranchIdAndProductTypePriceIdAndCreatedAtBetween(Timestamp from, Timestamp to, UUID productTypePriceId, UUID branchId);
}
