package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.LossProduct;

import java.util.List;
import java.util.UUID;

public interface LossProductRepository extends JpaRepository<LossProduct, UUID> {
    List<LossProduct> findAllByLossId(UUID lossId);
}
