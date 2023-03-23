package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Warehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Optional<Warehouse> findByBranchIdAndProductId(UUID branchId, UUID productId);

    Page<Warehouse> findAllByProduct_BusinessIdAndAmountNotOrderByAmountAsc(UUID product_business_id, double amount, Pageable pageable);
    Page<Warehouse> findAllByBranch_BusinessIdAndAmountNotOrderByAmountAsc(UUID product_business_id, double amount, Pageable pageable);
    Page<Warehouse> findAllByBranchIdAndAmountNotOrderByAmountAsc(UUID branch_id, double amount, Pageable pageable);
    Optional<Warehouse> findByBranchIdAndProductTypePriceId(UUID branchId, UUID productTypePriceId);
    Optional<Warehouse> findByProductIdAndBranchId(UUID product_id, UUID branch_id);
    Optional<Warehouse> findByProductTypePriceIdAndBranchId(UUID productTypePrice_id, UUID branch_id);
    Optional<Warehouse> findByProduct_Id(UUID productId);
    List<Warehouse> findAllByProduct_Id(UUID productId);
    List<Warehouse> findAllByProduct_IdAndBranch_Id(UUID product_id, UUID branch_id);
    List<Warehouse> findAllByProductTypePrice_Id(UUID productTypePrice_id);
    List<Warehouse> findAllByProductTypePrice_IdAndBranch_Id(UUID productTypePrice_id, UUID product_branch_id);
    boolean existsByBranchIdAndProductId(UUID branchId, UUID productId);
    boolean existsByBranchIdAndProductTypePriceId(UUID branchId, UUID productTypePriceId);
}
