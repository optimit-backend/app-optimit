package uz.pdp.springsecurity.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.BalanceHistory;

import java.util.UUID;

public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, UUID> {
}
