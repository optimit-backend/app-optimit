package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.FifoCalculation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FifoCalculationRepository extends JpaRepository<FifoCalculation, UUID> {
    List<FifoCalculation> findAllByBranchIdAndProductIdAndActiveTrueOrderByDateAscCreatedAtAsc(UUID branchId, UUID productId);
    // asc and desc
//    List<FifoCalculation> findAllByBranchIdAndProductIdAndActiveTrueOrderByDateDescCreatedAtDesc(UUID branchId, UUID productId);


    List<FifoCalculation> findAllByBranchIdAndProductTypePriceIdAndActiveTrueOrderByDateAscCreatedAtAsc(UUID branchId, UUID productTypePriceId);
    // asc and desc

//    List<FifoCalculation> findAllByBranchIdAndProductTypePriceIdAndActiveTrueOrderByDateDescCreatedAtDesc(UUID branchId, UUID productTypePriceId);


    List<FifoCalculation> findAllByBranchIdAndProductIdOrderByDateDescCreatedAtDesc(UUID branchId, UUID productId);

    List<FifoCalculation> findAllByBranchIdAndProductTypePriceIdOrderByDateDescCreatedAtDesc(UUID branchId, UUID productId);

    Optional<FifoCalculation> findByPurchaseProductId(UUID purchaseProductId);
}
