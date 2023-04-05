package uz.pdp.springsecurity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.Task;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findAllByBranchId(UUID branch_id, Pageable pageable);
    List<Task> findAllByBranch_Id(UUID branch_id);
    Page<Task> findAllByTaskStatusId(UUID taskStatus_id, Pageable pageable);
}
