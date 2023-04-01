package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.TaskStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskStatusRepository extends JpaRepository<TaskStatus,UUID> {
    List<TaskStatus> findAllByBranchId(UUID branchId);
    Optional<TaskStatus> findByName(String name);
    List<TaskStatus> findAllByNameInOrBranchId(Collection<String> name, UUID branch_id);

    List<TaskStatus> findAllByOrderByRowNumber();

    long count();
}
