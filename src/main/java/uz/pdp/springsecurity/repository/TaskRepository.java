package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Task;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    int countByTaskStatusId(UUID taskStatus_id);
    int countByProjectIdAndTaskStatus_OrginalName(UUID project_id, String taskStatus_orginalName);
    int countByProjectId(UUID project_id);
    Page<Task> findAllByBranchId(UUID branch_id, Pageable pageable);
    List<Task> findAllByProjectId(UUID project_id);
    List<Task> findAllByBranch_Id(UUID branch_id);
    Page<Task> findAllByProject_Id(UUID project_id, Pageable pageable);
    Page<Task> findAllByTaskStatus_Id(UUID project_id, Pageable pageable);
    Page<Task> findAllByBranchIdAndCreatedAtBetween(UUID branch_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndProject_IdAndCreatedAtBetween(UUID branch_id, UUID project_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndProject_IdAndTaskTypeIdAndTaskStatus_IdAndCreatedAtBetween(UUID branch_id, UUID project_id, UUID taskType_id, UUID taskStatus_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndTaskTypeIdAndTaskStatus_IdAndCreatedAtBetween(UUID branch_id, UUID taskType_id, UUID taskStatus_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndProjectIdAndTaskStatus_IdAndCreatedAtBetween(UUID branch_id, UUID project_id, UUID taskStatus_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndTaskStatus_IdAndCreatedAtBetween(UUID branch_id, UUID taskStatus_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndTaskStatus_Id(UUID branch_id, UUID taskStatus_id, Pageable pageable);

    Page<Task> findAllByBranchIdAndProjectIdAndTaskTypeIdAndCreatedAtBetween(UUID branch_id, UUID project_id, UUID taskType_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);


    Page<Task> findAllByBranchIdAndTaskTypeId(UUID branchId, UUID typeId, Pageable pageable);

    Page<Task> findAllByBranchIdAndTaskTypeIdAndCreatedAtBetween(UUID branchId, UUID typeId, Timestamp start, Timestamp end, Pageable pageable);

    Page<Task> findAllByBranchIdAndProjectId(UUID branchId, UUID projectId, Pageable pageable);

    Page<Task> findAllByBranchIdAndProjectIdAndTaskTypeId(UUID branchId, UUID projectId, UUID typeId, Pageable pageable);

    Page<Task> findAllByBranchIdAndProjectIdAndCreatedAtBetween(UUID branchId, UUID projectId, Timestamp start, Timestamp end, Pageable pageable);
}
