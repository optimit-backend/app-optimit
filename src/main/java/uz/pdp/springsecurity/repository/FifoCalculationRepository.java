package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.springsecurity.entity.FifoCalculation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FifoCalculationRepository extends JpaRepository<FifoCalculation, UUID> {
    List<FifoCalculation> findAllByBranchIdAndProductIdAndActiveTrueOrderByDate(UUID branchId, UUID productId);

    List<FifoCalculation> findAllByBranchIdAndProductTypePriceIdAndActiveTrueOrderByDate(UUID branchId, UUID productTypePriceId);

    List<FifoCalculation> findFirst20ByBranchIdAndProductIdOrderByDateDesc(UUID branchId, UUID productId);

    List<FifoCalculation> findFirst20ByBranchIdAndProductTypePriceIdOrderByDateDesc(UUID branchId, UUID productId);

    Optional<FifoCalculation> findByPurchaseProductId(UUID purchaseProductId);

    List<FifoCalculation> findAllByBranchIdAndActiveTrue(UUID branchId);

    List<FifoCalculation> findAllByBranch_BusinessIdAndActiveTrue(UUID businessId);

    Page<FifoCalculation> findAllByBranchIdAndProductIdAndProductionIsNotNullOrderByCreatedAtDesc(UUID branchId, UUID productId, Pageable pageable);
    Page<FifoCalculation> findAllByBranchIdAndProductTypePriceIdAndProductionIsNotNullOrderByCreatedAtDesc(UUID branchId, UUID productTypePriceId, Pageable pageable);

    @Query(value = "SELECT remain_amount FROM fifo_calculation WHERE purchase_product_id = ?1", nativeQuery = true)
    Double remainQuantityByPurchaseProductId(UUID purchaseProductId);

    void deleteAllByProductId(UUID productId);
    void deleteAllByProductTypePrice_ProductId(UUID productId);
}
