package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.springsecurity.entity.Product;
import uz.pdp.springsecurity.enums.Type;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByBarcodeAndBusinessIdAndActiveTrue(String barcode, UUID businessId);
    boolean existsByBarcodeAndBusinessIdAndIdIsNotAndActiveTrue(String barcode, UUID businessId, UUID productId);
    Page<Product> findAllByBranchIdAndNameContainingIgnoreCaseOrBarcodeContainingIgnoreCase(UUID branch_id, String name, String barcode, Pageable pageable);
    Page<Product> findAllByBranch_BusinessIdAndNameContainingIgnoreCaseOrBarcodeContainingIgnoreCase(UUID branch_id, String name, String barcode, Pageable pageable);
    List<Product> findAllByBrandIdAndCategoryIdAndBranchIdAndActiveTrue(UUID brand_id, UUID category_id, UUID branchId);
    Page<Product> findAllByBrand_IdAndCategoryIdAndBranchIdAndActiveTrue(UUID brand_id, UUID category_id, UUID branchId,Pageable pageable);
    List<Product> findAllByBrandIdAndActiveIsTrue(UUID brand_id);
    Optional<Product> findByIdAndBranchIdAndActiveTrue(UUID id, UUID branchId);
    Optional<Product> findAllByBarcodeAndBranchIdAndActiveTrue(String barcode, UUID branch_id);
    List<Product> findAllByCategoryIdAndBranchIdAndActiveTrue(UUID category_id, UUID branch_id);
    Page<Product> findAllByCategoryIdAndBranch_IdAndActiveTrue(UUID category_id, UUID branch_id,Pageable pageable);
    List<Product> findAllByBrandIdAndBusinessIdAndActiveTrue(UUID brand_id, UUID businessId);
    Page<Product> findAllByBrand_IdAndBusinessIdAndActiveTrue(UUID brand_id, UUID businessId,Pageable pageable);
    List<Product> findAllByBranchIdAndActiveIsTrue(UUID branch_id);
    List<Product> findAllByBranchIdAndActiveIsTrueAndNameContainingIgnoreCase(UUID branch_id, String name);
    List<Product> findAllByBranchIdAndActiveIsTrueAndBarcodeContainingIgnoreCase(UUID branch_id, String name);
    Page<Product> findAllByActiveIsTrueAndBranchId(UUID branch_id, Pageable pageable);
    List<Product> findAllByBranchIdAndBarcodeOrNameAndActiveTrue(UUID branch_id, String barcode, String name);
    Optional<Product> findByBarcodeAndBranch_IdAndActiveTrue(String barcode, UUID receivedBranch);
    List<Product> findAllByBusiness_IdAndActiveTrue(UUID businessId);
    Page<Product> findAllByBusinessIdAndActiveTrue(UUID businessId,Pageable pageable);
    List<Product> findAllByBranchIdAndActiveTrue(UUID branch_id);
    Page<Product> findAllByBranch_IdAndActiveTrue(UUID branch_id,Pageable pageable);
    List<Product> findAllByBranch_BusinessIdAndActiveTrue(UUID branch_business_id);
    List<Product> findAllByCategoryIdAndBusinessIdAndActiveTrue(UUID categoryId, UUID businessId);
    Page<Product> findAllByCategoryIdAndBusiness_IdAndActiveTrue(UUID categoryId, UUID businessId,Pageable pageable);
    List<Product> findAllByBrandIdAndBranchIdAndActiveTrue(UUID brandId, UUID branchId);
    Page<Product> findAllByBrand_IdAndBranchIdAndActiveTrue(UUID brandId, UUID branchId,Pageable pageable);
    List<Product> findAllByBrandIdAndCategoryIdAndBusinessIdAndActiveTrue(UUID brandId, UUID categoryId, UUID businessId);
    Page<Product> findAllByBrandIdAndCategoryIdAndBusiness_IdAndActiveTrue(UUID brandId, UUID categoryId, UUID businessId,Pageable pageable);
    List<Product> findAllByBusinessIdAndActiveTrueAndBuyDollarTrueOrSaleDollarTrue(UUID businessId);
    int countAllByBranchId(UUID branchId);
}
