package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface TaskStatusRepository extends JpaRepository<TaskStatus,UUID> {
    List<TaskStatus> findAllByBusiness_Id(UUID business_id);

    List<TaskStatus> findAllByOrderByRowNumber();

    long count();
}
