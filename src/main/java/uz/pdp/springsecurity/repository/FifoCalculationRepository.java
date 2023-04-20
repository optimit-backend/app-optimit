package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
