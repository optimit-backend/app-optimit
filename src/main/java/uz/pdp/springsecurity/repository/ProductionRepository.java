package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Production;

import java.util.List;
import java.util.UUID;

public interface ProductionRepository extends JpaRepository<Production, UUID> {
    List<Production> findAllByBranchId(UUID branchId);
    List<Production> findAllByProduct_CategoryIdAndProduct_BrandIdAndProduct_BranchId(UUID product_category_id, UUID product_brand_id, UUID product_branch_business_id);
    List<Production> findAllByProduct_CategoryIdAndProduct_BranchId(UUID product_category_id,UUID product_branch_business_id);
    List<Production> findAllByProduct_BrandIdAndProduct_BranchId(UUID product_category_id,UUID product_branch_business_id);
    List<Production> findAllByProduct_BranchId(UUID branchId);
}
