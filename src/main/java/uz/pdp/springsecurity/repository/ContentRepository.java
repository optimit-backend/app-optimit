package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Content;

import java.util.List;
import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {
    List<Content> findAllByBranchId(UUID branchId);
}
