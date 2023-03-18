package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.ProductAmount;

import java.util.UUID;

public interface ProductAmountRepository extends JpaRepository<ProductAmount, UUID> {
}
