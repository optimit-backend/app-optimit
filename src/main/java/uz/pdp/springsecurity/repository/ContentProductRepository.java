package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.ContentProduct;

import java.util.List;
import java.util.UUID;

public interface ContentProductRepository extends JpaRepository<ContentProduct, UUID> {
    List<ContentProduct> findAllByContentId(UUID contentId);
    List<ContentProduct> findAllByProductionId(UUID productionId);
    Page<ContentProduct> findAllByProduction_BranchIdAndProductIdAndProductionIsNotNullAndByProductIsFalseAndLossProductIsFalseOrderByCreatedAtDesc(UUID branchId, UUID productID, Pageable pageable);
    Page<ContentProduct> findAllByProduction_BranchIdAndProductTypePriceIdAndProductionIsNotNullAndByProductIsFalseAndLossProductIsFalseOrderByCreatedAtDesc(UUID branchId, UUID productTypePriceID, Pageable pageable);

    void deleteAllByContentId(UUID contentId);
}
