package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.CurrentCource;

import java.util.UUID;

public interface CurrentCourceRepository extends JpaRepository<CurrentCource, UUID> {

    CurrentCource getByCurrencyId(UUID currency_id);

    CurrentCource getByCurrencyIdAndActive(UUID currency_id, boolean active);

}
