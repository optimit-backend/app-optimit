package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProjectDto;
import uz.pdp.springsecurity.repository.*;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

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

    @Autowired
    ProjectStatusRepository projectStatusRepository;

    @Autowired
    FileDateRepository fileDateRepository;
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
        if (projectDto.getProjectTypeId()!=null){
            Optional<ProjectType> optionalProjectType = projectTypeRepository.findById(projectDto.getProjectTypeId());
            optionalProjectType.ifPresent(project::setProjectType);
        }
        project.setCustomer(optionalCustomer.get());
        project.setDescription(projectDto.getDescription());

        List<User> userList = new ArrayList<>();
        for (UUID uuid : projectDto.getUserList()) {
            Optional<User> optionalUser = userRepository.findById(uuid);
            optionalUser.ifPresent(userList::add);
        }
        project.setUsers(userList);

        List<FileData> fileDataList = new ArrayList<>();
        if (projectDto.getFileDateList()!=null){
            for (UUID uuid : projectDto.getFileDateList()) {
                Optional<FileData> optionalFileData = fileDateRepository.findById(uuid);
                if (optionalFileData.isPresent()){
                    FileData fileData = optionalFileData.get();
                    fileDataList.add(fileData);
                }
            }
        }

        project.setFileDataList(fileDataList);
        project.setBudget(projectDto.getBudget());
        Optional<Stage> optionalStage = stageRepository.findById(projectDto.getStageId());
        optionalStage.ifPresent(project::setStage);
        project.setGoalAmount(projectDto.getGoalAmount());
        project.setProduction(projectDto.isProduction());
        project.setBranch(optionalBranch.get());
        projectRepository.save(project);

        return new ApiResponse("Added",true,project);
    }

    public ApiResponse edit(UUID id, ProjectDto projectDto) {
        Optional<Project> optionalProject = projectRepository.findById(id);
        if (optionalProject.isEmpty()){
            return new ApiResponse("Project Not Found",false);
        }
        Project project = optionalProject.get();
        project.setName(projectDto.getName());
        project.setStartDate(projectDto.getStartDate());
        project.setEndDate(projectDto.getEndDate());
        project.setDeadline(projectDto.getDeadline());

        Optional<ProjectType> optionalProjectType = projectTypeRepository.findById(projectDto.getProjectTypeId());
        optionalProjectType.ifPresent(project::setProjectType);

        Optional<Customer> optionalCustomer = customerRepository.findById(projectDto.getCustomerId());
        optionalCustomer.ifPresent(project::setCustomer);

        project.setDescription(projectDto.getDescription());
        List<User> userList = new ArrayList<>();
        if (!projectDto.getUserList().isEmpty()) {
            for (UUID uuid : projectDto.getUserList()) {
                Optional<User> optionalUser = userRepository.findById(uuid);
                optionalUser.ifPresent(userList::add);
            }
        }
        project.setUsers(userList);

        List<FileData> fileDataList= new ArrayList<>();
        if (!projectDto.getFileDateList().isEmpty()) {
            for (UUID uuid : projectDto.getFileDateList()) {
                Optional<FileData> optionalAttachment = fileDateRepository.findById(uuid);
                optionalAttachment.ifPresent(fileDataList::add);
            }
        }
        project.setFileDataList(fileDataList);

        project.setBudget(projectDto.getBudget());

        Optional<Stage> optionalStage = stageRepository.findById(projectDto.getStageId());
        optionalStage.ifPresent(project::setStage);

        project.setGoalAmount(projectDto.getGoalAmount());
        project.setProduction(projectDto.isProduction());

        Optional<Branch> optionalBranch = branchRepository.findById(projectDto.getBranchId());
        optionalBranch.ifPresent(project::setBranch);
        projectRepository.save(project);

        return new ApiResponse("Edited",true);
    }

    public ApiResponse get(UUID id) {
        Optional<Project> optionalProject = projectRepository.findById(id);
        return optionalProject.map(project -> new ApiResponse("Found",true,project)).orElseGet(() -> new ApiResponse("Project Not Found", false));
    }

    public ApiResponse delete(UUID id) {
        boolean exists = projectRepository.existsById(id);
        if (!exists){
            return new ApiResponse("Project Not Found",false);
        }
        projectRepository.deleteById(id);
        return new ApiResponse("Deleted",true);
    }

    public ApiResponse  getAllByBranchId(UUID branchId, int page, int size) {
        if (page == 0 && size == 0){
            List<Project> projectList = projectRepository.findAllByBranch_Id(branchId);
            if (!projectList.isEmpty()){
                return new ApiResponse("Found",true,projectList);
            }
        }
        Pageable pageable = PageRequest.of(page,size);
        Page<Project> projects = projectRepository.findAllByBranchId(branchId, pageable);
        if (projects.isEmpty()){
            assert projects.getTotalElements() <= 0 : "list is empty";
            return new ApiResponse("Project Not Found",false);
        }

        return new ApiResponse("Found",true,projects);
    }

    public ApiResponse findByStatusId(UUID statusId) {
        List<Project> projects = projectRepository.findAllByStageId(statusId);
        if (projects.isEmpty()){
            return new ApiResponse("Projects Not Found",false);
        }
        return new ApiResponse("Found",true,projects);
    }


}
