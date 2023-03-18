package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.PurchaseProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseProductRepository extends JpaRepository<PurchaseProduct, UUID> {
    List<PurchaseProduct> findAllByPurchaseId(UUID purchaseId);
}
