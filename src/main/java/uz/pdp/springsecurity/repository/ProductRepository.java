package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Product;
import uz.pdp.springsecurity.enums.Type;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByBarcodeAndBusinessIdAndActiveTrue(String barcode, UUID businessId);
    boolean existsByBarcodeAndBusinessIdAndIdIsNotAndActiveTrue(String barcode, UUID businessId, UUID productId);

    List<Product> findAllByBrandIdAndCategoryIdAndBranchIdAndActiveTrue(UUID brand_id, UUID category_id, UUID business_id);

    List<Product> findAllByBrandIdAndActiveIsTrue(UUID brand_id);

    Optional<Product> findByIdAndBranchIdAndActiveTrue(UUID id, UUID branchId);

    Optional<Product> findAllByBarcodeAndBranchIdAndActiveTrue(String barcode, UUID branch_id);

    List<Product> findAllByCategoryIdAndBranchIdAndActiveTrue(UUID category_id, UUID branch_id);

    List<Product> findAllByBrandIdAndBranchIdAndActiveTrue(UUID brand_id, UUID branch_id);
    List<Product> findAllByBrandIdAndBusinessIdAndActiveTrue(UUID brand_id, UUID businessId);

    List<Product> findAllByBranchIdAndActiveIsTrue(UUID branch_id);

    List<Product> findAllByBranchIdAndBarcodeOrNameAndActiveTrue(UUID branch_id, String barcode, String name);

    Optional<Product> findByBarcodeAndBranch_IdAndActiveTrue(String barcode, UUID receivedBranch);

    List<Product> findAllByBusiness_IdAndActiveTrue(UUID businessId);
    List<Product> findAllByBranchIdAndActiveTrue(UUID branch_id);

    List<Product> findAllByCategoryIdAndBusinessIdAndActiveTrue(UUID categoryId, UUID businessId);

    List<Product> findAllByBusiness_IdAndActiveIsTrueAndTypeLike(UUID business_id, Type type);
}
