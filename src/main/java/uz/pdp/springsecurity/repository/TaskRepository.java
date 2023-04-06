package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Task;
import uz.pdp.springsecurity.entity.TaskStatus;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    int countByTaskStatusId(UUID taskStatus_id);
    int countByProjectIdAndTaskStatus_OrginalName(UUID project_id, String taskStatus_orginalName);
    int countByProjectId(UUID project_id);

    Page<Task> findAllByBranchId(UUID branch_id, Pageable pageable);
    List<Task> findAllByBranch_Id(UUID branch_id);
    Page<Task> findAllByProjectId(UUID project_id,Pageable pageable);
    Page<Task> findAllByTaskStatusId(UUID taskStatus_id, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndTaskTypeId(UUID taskStatus_id, UUID taskType_id, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndTaskTypeIdAndCreatedAtBetween(UUID taskStatus_id, UUID taskType_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndTaskTypeIdAndProjectIdAndCreatedAtBetween(UUID taskStatus_id, UUID taskType_id, UUID project_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndTaskTypeIdAndProjectId(UUID taskStatus_id, UUID taskType_id, UUID project_id, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndProjectIdAndCreatedAtBetween(UUID taskStatus_id, UUID project_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndCreatedAtBetween(UUID taskStatus_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndProjectId(UUID taskStatus_id,UUID projectId, Pageable pageable);
    Page<Task> findAllByProject_Id(UUID project_id, Pageable pageable);
    Page<Task> findAllByTaskStatus_Id(UUID project_id, Pageable pageable);
    List<Task> findAllByTaskStatusIdAndName(UUID taskStatus_id, String name);
    Page<Task> findAllByBranchIdAndCreatedAtBetween(UUID branch_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndProject_IdAndCreatedAtBetween(UUID branch_id, UUID project_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndProject_IdAndTaskTypeIdAndTaskStatus_IdAndCreatedAtBetween(UUID branch_id, UUID project_id, UUID taskType_id, UUID taskStatus_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndTaskTypeIdAndTaskStatus_IdAndCreatedAtBetween(UUID branch_id, UUID taskType_id, UUID taskStatus_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndProjectIdAndTaskStatus_IdAndCreatedAtBetween(UUID branch_id, UUID project_id, UUID taskStatus_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndTaskStatus_IdAndCreatedAtBetween(UUID branch_id, UUID taskStatus_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
    Page<Task> findAllByBranchIdAndTaskStatus_Id(UUID branch_id, UUID taskStatus_id, Pageable pageable);

}
