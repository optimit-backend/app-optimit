package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Bonus;

import java.util.UUID;

public interface BonusRepository extends JpaRepository<Bonus, UUID> {
}
