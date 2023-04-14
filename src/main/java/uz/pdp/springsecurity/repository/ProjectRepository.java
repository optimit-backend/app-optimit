package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Attachment;
import uz.pdp.springsecurity.entity.Project;
import uz.pdp.springsecurity.entity.ProjectType;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.payload.ApiResponse;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByProjectTypeId(UUID projectType_id);

    Page<Project> findAllByBranchId(UUID branchId, Pageable pageable);

    List<Project> findAllByBranch_Id(UUID branch_id);

//    List<Project> findAllByStageId(UUID projectStatus_id);
//    Page<Project> findAllByBranchIdAndCustomerId(UUID branch_id, UUID customer_id, Pageable pageable);
//    Page<Project> findAllByBranchIdAndProjectTypeId(UUID branch_id, UUID stage_id, Pageable pageable);
//    Page<Project> findAllByBranchIdAndCreatedAtBetween(UUID branch_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndCustomerId(UUID branch_id, UUID projectType_id, UUID stage_id, UUID customer_id, Pageable pageable);
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndCustomerIdAndCreatedAtBetween(UUID branchId, UUID typeId, UUID customerId, Timestamp start, Timestamp end, Pageable pageable);
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndCreatedAtBetween(UUID branchId, UUID typeId, Timestamp start, Timestamp end, Pageable pageable);
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndCustomerId(UUID branchId, UUID typeId, UUID customerId, Pageable pageable);
//    Page<Project> findAllByBranchIdAndCustomerIdAndCreatedAtBetween(UUID branchId, UUID customerId, Timestamp start, Timestamp end, Pageable pageable);
//    Page<Project> findAllByBranchIdAndStageIdAndCreatedAtBetween(UUID branch_id, UUID stage_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndCustomerId(UUID branch_id, UUID customer_id, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndStageId(UUID branch_id, UUID stage_id, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndProjectTypeId(UUID branch_id, UUID stage_id, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndCreatedAtBetween(UUID branch_id, Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
//
//
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndStageIdAndCustomerIdAndCreatedAtBetween(UUID branchId, UUID typeId, UUID stageId, UUID customerId, Timestamp start, Timestamp end, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndStageIdAndCustomerId(UUID branch_id, UUID projectType_id, UUID stage_id, UUID customer_id, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndCustomerIdAndCreatedAtBetween(UUID branchId, UUID typeId, UUID customerId, Timestamp start, Timestamp end, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndStageIdAndCreatedAtBetween(UUID branchId, UUID typeId, UUID stageId, Timestamp start, Timestamp end, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndStageIdAndCustomerIdAndCreatedAtBetween(UUID branchId, UUID stageId, UUID customerId, Timestamp start, Timestamp end, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndCustomerId(UUID branchId, UUID typeId, UUID customerId, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndStageId(UUID branchId, UUID typeId, UUID stageId, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndProjectTypeIdAndCreatedAtBetween(UUID branchId, UUID typeId, Timestamp start, Timestamp end, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndStageIdAndCustomerId(UUID branchId, UUID stageId, UUID customerId, Pageable pageable);
//
//    Page<Project> findAllByBranchIdAndCustomerIdAndCreatedAtBetween(UUID branchId, UUID customerId, Timestamp start, Timestamp end, Pageable pageable);

    List<Project> findAllByUsersId(UUID users_id);

}
