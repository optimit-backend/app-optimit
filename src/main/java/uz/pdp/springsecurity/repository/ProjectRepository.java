package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Attachment;
import uz.pdp.springsecurity.entity.Project;
import uz.pdp.springsecurity.entity.ProjectType;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByStageId(UUID projectStatus_id);
    Page<Project> findAllByBranchId(UUID branchId, Pageable pageable);
    Page<Project> findAllByBranchIdAndStageId(UUID branchId, UUID stageId, Pageable pageable);
    Page<Project> findAllByBranchIdAndStageIdAndCreatedAtBetween(UUID branch_id, UUID stage_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Project> findAllByBranchIdAndCreatedAtBetween(UUID branch_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Project> findAllByBranchIdAndStageIdAndProjectTypeIdAndCreatedAtBetween(UUID branch_id, UUID stage_id, UUID typeId, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Project> findAllByBranchIdAndStageIdAndProjectTypeId(UUID branch_id, UUID stage_id, UUID typeId, Pageable pageable);
    List<Project> findAllByBranch_Id(UUID branch_id);
}
