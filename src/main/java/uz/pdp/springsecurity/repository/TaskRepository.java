package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Task;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    int countAllByProjectId(UUID project_id);

    int countAllByUsersId(UUID users_id);
    int countAllByUsersIdAndTaskStatus_OrginalName(UUID users_id, String taskStatus_orginalName);
    int countAllByUsersIdAndExpiredIsTrue(UUID users_id);
    int countByTaskStatusId(UUID taskStatus_id);
    int countByProjectIdAndTaskStatus_OrginalName(UUID project_id, String taskStatus_orginalName);
    int countByProjectId(UUID project_id);
    int countAllByTaskStatus_OrginalNameAndProjectId(String taskStatus_orginalName, UUID project_id);
    int countAllByProjectIdAndExpiredTrue(UUID project_id);
    int countAllByStageId(UUID stage_id);
    int countAllByStageIdAndTaskStatus_OrginalName(UUID stage_id, String taskStatus_orginalName);
    Page<Task> findAllByBranch_Id(UUID branch_id, Pageable pageable);
    Page<Task> findAllByProjectIdAndTaskStatusIdAndTaskTypeIdAndExpiredTrue(UUID project_id, UUID taskStatus_id, UUID taskType_id, Pageable pageable);
    List<Task> findAllByProjectId(UUID project_id);
    Page<Task> findAllByProject_Id(UUID project_id,Pageable pageable);
    List<Task> findAllByBranchId(UUID branch_id);
    List<Task> findByNameContainingIgnoreCase(String name);
    Page<Task> findAllByTaskStatus_Id(UUID project_id, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndProjectIdAndTaskTypeIdAndExpiredTrue(UUID taskStatus_id, UUID project_id, UUID taskType_id, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndProjectIdAndTaskTypeId(UUID id, UUID projectId, UUID typeId, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndProjectIdAndExpiredTrue(UUID id, UUID projectId, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndProject_Id(UUID id, UUID projectId, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndTaskTypeId(UUID id, UUID typeId, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndExpiredTrue(UUID id, Pageable pageable);
    Page<Task> findAllByTaskStatusIdAndTaskTypeIdAndExpiredTrue(UUID id, UUID typeId, Pageable pageable);

    Page<Task> findAllByProjectIdAndTaskTypeIdAndExpiredTrue(UUID projectId, UUID typeId, Pageable pageable);

    Page<Task> findAllByTaskTypeId(UUID taskType_id, Pageable pageable);

    Page<Task> findAllByBranch_IdAndExpiredTrue(UUID branch_id, Pageable pageable);

    Page<Task> findAllByTaskStatusId(UUID statusId, Pageable pageable);

    Page<Task> findAllByProjectIdAndExpiredTrue(UUID projectId, Pageable pageable);
}
