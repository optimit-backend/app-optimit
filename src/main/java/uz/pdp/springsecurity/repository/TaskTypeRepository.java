package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.TaskType;

import java.util.List;
import java.util.UUID;

public interface TaskTypeRepository extends JpaRepository<TaskType, UUID> {
    List<TaskType> findAllByBusiness_Id(UUID business_id);
}