package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.FifoCalculation;
import uz.pdp.springsecurity.entity.PurchaseProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FifoCalculationRepository extends JpaRepository<FifoCalculation, UUID> {
    List<FifoCalculation> findAllByBranchIdAndProductIdAndActiveTrueOrderByDateAscCreatedAtAsc(UUID branchId, UUID productId);

    List<FifoCalculation> findAllByBranchIdAndProductTypePriceIdAndActiveTrueOrderByDateAscCreatedAtAsc(UUID branchId, UUID productId);

    Optional<FifoCalculation> findByPurchaseIdAndProductId(UUID purchaseId, UUID productId);

    Optional<FifoCalculation> findByPurchaseIdAndProductTypePriceId(UUID purchaseId, UUID productTypePriceId);
}
