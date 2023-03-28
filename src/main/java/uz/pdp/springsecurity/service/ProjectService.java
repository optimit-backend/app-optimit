package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProjectDto;
import uz.pdp.springsecurity.repository.*;

import java.util.ArrayList;
import java.util.List;
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
    UserRepository userRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    StageRepository stageRepository;

    @Autowired
    AttachmentRepository attachmentRepository;
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

        List<User> userList = new ArrayList<>();
        for (UUID uuid : projectDto.getUserList()) {
            Optional<User> optionalUser = userRepository.findById(uuid);
            optionalUser.ifPresent(userList::add);
        }
        project.setUsers(userList);
        List<Attachment> attachmentList=new ArrayList<>();
        for (UUID uuid : projectDto.getAttachmentList()) {
            Optional<Attachment> optionalAttachment = attachmentRepository.findAllById(uuid);
            optionalAttachment.ifPresent(attachmentList::add);
        }
        project.setAttachmentList(attachmentList);
        project.setBudget(projectDto.getBudget());
        Optional<Stage> optionalStage = stageRepository.findById(projectDto.getStageId());
        optionalStage.ifPresent(project::setStage);
        project.setGoalAmount(projectDto.getGoalAmount());
        project.setProduction(projectDto.isProduction());
        project.setBranch(optionalBranch.get());

        return new ApiResponse("Added",true,project);
    }

    public ApiResponse edit(UUID id, ProjectDto projectDto) {

        return new ApiResponse("Edited",true);
    }

    public ApiResponse get(UUID id) {
        Optional<Project> optionalProject = projectRepository.findById(id);
        return optionalProject.map(project -> new ApiResponse("Found", true, project)).orElseGet(() -> new ApiResponse("Project Not Found", false));
    }

    public ApiResponse delete(UUID id) {
        return new ApiResponse("Deleted",true);
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        return new ApiResponse("Found",true);
    }
}
