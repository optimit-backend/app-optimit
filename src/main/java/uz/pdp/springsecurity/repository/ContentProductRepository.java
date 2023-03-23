package uz.pdp.springsecurity.repository;

import org.bouncycastle.LICENSE;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Content;
import uz.pdp.springsecurity.entity.ContentProduct;

import java.util.List;
import java.util.UUID;

public interface ContentProductRepository extends JpaRepository<ContentProduct, UUID> {
    List<ContentProduct> findAllByContentId(UUID contentId);

    List<ContentProduct> findAllByProductionId(UUID productionId);
}
