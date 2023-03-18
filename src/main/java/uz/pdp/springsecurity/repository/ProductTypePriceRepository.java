package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.ProductTypePrice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductTypePriceRepository extends JpaRepository<ProductTypePrice, UUID> {
    List<ProductTypePrice> findAllByProductId(UUID product_id);
}
