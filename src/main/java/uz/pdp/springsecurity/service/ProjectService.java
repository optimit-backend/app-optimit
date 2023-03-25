package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProjectDto;
import uz.pdp.springsecurity.repository.*;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    BonusRepository bonusRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    StageRepository stageRepository;

    @Autowired
    ProjectTypeRepository projectTypeRepository;
    public ApiResponse add(ProjectDto projectDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(projectDto.getBranchId());
        if (optionalBranch.isEmpty()){
            return new ApiResponse("Branch Not Found",false);
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(projectDto.getCustomerId());
        if (optionalCustomer.isEmpty()){
            return new ApiResponse("Customer Not Found",false);
        }

        Project project = new Project();
        project.setName(projectDto.getName());
        project.setStartDate(projectDto.getStartDate());
        project.setEndDate(projectDto.getEndDate());
        project.setDeadline(projectDto.getDeadline());
        Optional<ProjectType> optionalProjectType = projectTypeRepository.findById(projectDto.getProjectTypeId());
        optionalProjectType.ifPresent(project::setProjectType);
        project.setCustomer(optionalCustomer.get());
        project.setDescription(projectDto.getDescription());
        project.setUsers(projectDto.getUserList());
        if (!projectDto.getAttachmentList().isEmpty()){
            project.setAttachmentList(projectDto.getAttachmentList());
        }
        project.setBudget(projectDto.getBudget());
        Optional<Stage> optionalStage = stageRepository.findById(projectDto.getStageId());
        optionalStage.ifPresent(project::setStage);
        project.setGoalAmount(projectDto.getGoalAmount());
        project.setProduction(projectDto.isProduction());
        Optional<Bonus> optionalBonus = bonusRepository.findById(projectDto.getBonus().getId());
        optionalBonus.ifPresent(project::setBonus);
        project.setBranch(optionalBranch.get());

        return new ApiResponse("Added",true);
    }

    public ApiResponse edit(UUID id, ProjectDto projectDto) {

        return new ApiResponse("Edited",true);
    }

    public ApiResponse get(UUID id) {
        return new ApiResponse("Found",true);
    }

    public ApiResponse delete(UUID id) {
        return new ApiResponse("Deleted",true);
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        return new ApiResponse("Found",true);
    }
}
