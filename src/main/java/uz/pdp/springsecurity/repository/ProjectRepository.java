package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Attachment;
import uz.pdp.springsecurity.entity.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByStageId(UUID projectStatus_id);
    Page<Project> findAllByBranchId(UUID branchId, Pageable pageable);
    List<Project> findAllByBranch_Id(UUID branch_id);
}
