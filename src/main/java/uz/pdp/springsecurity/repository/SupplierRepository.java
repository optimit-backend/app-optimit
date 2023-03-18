package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.springsecurity.entity.Supplier;

import java.util.List;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier,UUID> {

//    @Query(value = "select * from supplier s where s.business_id = ?",nativeQuery = true)
//    List<Supplier> findAllByBusinessId(UUID businessId);

    List<Supplier> findAllByBusinessId(UUID business_id);
}
