package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Project;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByProjectTypeId(UUID projectType_id);
    Page<Project> findAllByBranchId(UUID branchId, Pageable pageable);

    List<Project> findAllByBranch_Id(UUID branch_id);
    List<Project> findAllByUsersId(UUID users_id);
    Page<Project> findAllByProjectTypeIdAndCustomerIdAndProjectStatusIdAndExpiredTrue(UUID projectType_id, UUID customer_id, UUID projectStatus_id, Pageable pageable);

    Page<Project> findAllByProjectTypeIdAndCustomerIdAndProjectStatusIdAndExpiredFalse(UUID projectType_id, UUID customer_id, UUID projectStatus_id, Pageable pageable);

    Page<Project> findAllByProjectTypeIdAndCustomerIdAndProjectStatusId(UUID branchId, UUID typeId, UUID customerId, UUID projectStatusId, Pageable pageable);

    Page<Project> findAllByProjectTypeIdAndCustomerIdAndExpiredTrue(UUID branchId, UUID typeId, UUID customerId, Pageable pageable);

    Page<Project> findAllByProjectTypeIdAndCustomerId(UUID branchId, UUID typeId, UUID customerId, Pageable pageable);

    Page<Project> findAllByProjectTypeIdAndProjectStatusId(UUID branchId, UUID typeId, UUID projectStatusId, Pageable pageable);

    Page<Project> findAllByProjectTypeIdAndExpiredTrue(UUID branchId, UUID typeId, Pageable pageable);

    Page<Project> findAllByProjectTypeId(UUID branchId, UUID typeId, Pageable pageable);

    Page<Project> findAllByCustomerIdAndProjectStatusIdAndExpiredTrue(UUID branchId, UUID customerId, UUID projectStatusId, Pageable pageable);

    Page<Project> findAllByCustomerIdAndProjectStatusId(UUID branchId, UUID customerId, UUID projectStatusId, Pageable pageable);

    Page<Project> findAllByCustomerIdAndExpiredTrue(UUID branchId, UUID customerId, Pageable pageable);

    Page<Project> findAllByCustomerId(UUID branchId, UUID customerId, Pageable pageable);

    Page<Project> findAllByProjectStatusIdAndExpiredTrue(UUID branchId, UUID projectStatusId, Pageable pageable);


    Page<Project> findAllByProjectStatusId(UUID branchId, UUID projectStatusId, Pageable pageable);

    Page<Project> findAllByExpiredTrue(UUID branchId, Pageable pageable);
}
