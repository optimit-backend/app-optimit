package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.springsecurity.entity.LidStatus;
import uz.pdp.springsecurity.entity.ProjectType;

import java.util.List;
import java.util.UUID;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, UUID> {

    List<ProjectType> findAllByBusinessId(UUID business_id);
}
