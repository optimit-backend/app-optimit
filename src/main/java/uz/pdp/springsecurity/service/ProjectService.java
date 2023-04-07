package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.NotificationType;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProjectDto;
import uz.pdp.springsecurity.repository.*;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
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
    TaskRepository taskRepository;

    @Autowired
    FileDateRepository fileDateRepository;
    private final NotificationRepository notificationRepository;


    public ApiResponse add(ProjectDto projectDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(projectDto.getBranchId());
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(projectDto.getCustomerId());
        if (optionalCustomer.isEmpty()) {
            return new ApiResponse("Customer Not Found", false);
        }

        Project project = new Project();
        project.setName(projectDto.getName());
        project.setStartDate(projectDto.getStartDate());
        project.setEndDate(projectDto.getEndDate());
        project.setDeadline(projectDto.getDeadline());
        if (projectDto.getProjectTypeId() != null) {
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
        if (projectDto.getFileDateList() != null) {
            for (UUID uuid : projectDto.getFileDateList()) {
                Optional<FileData> optionalFileData = fileDateRepository.findById(uuid);
                if (optionalFileData.isPresent()) {
                    FileData fileData = optionalFileData.get();
                    fileDataList.add(fileData);
                }
            }
        }

        project.setFileDataList(fileDataList);
        project.setBudget(projectDto.getBudget());
        if (projectDto.getStageId() != null) {
            Optional<Stage> optionalStage = stageRepository.findById(projectDto.getStageId());
            optionalStage.ifPresent(project::setStage);
        }
        project.setGoalAmount(projectDto.getGoalAmount());
        project.setProduction(projectDto.isProduction());
        project.setBranch(optionalBranch.get());
        projectRepository.save(project);

        List<User> users = project.getUsers();
        for (User user : users) {
            Notification notification = new Notification();
            notification.setRead(false);
            notification.setName("Sizga yangi project belgilandi!");
            notification.setMessage("Project ko'rishingiz mumkin!");
            notification.setType(NotificationType.NEW_PROJECT);
            notification.setObjectId(project.getId());
            notification.setUserTo(user);
            notificationRepository.save(notification);
        }

        return new ApiResponse("Added", true);
    }

    public ApiResponse edit(UUID id, ProjectDto projectDto) {
        Optional<Project> optionalProject = projectRepository.findById(id);
        if (optionalProject.isEmpty()) {
            return new ApiResponse("Project Not Found", false);
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

        List<FileData> fileDataList = new ArrayList<>();
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

        return new ApiResponse("Edited", true);
    }

    public ApiResponse get(UUID id) {
        Optional<Project> optionalProject = projectRepository.findById(id);
        return optionalProject.map(project -> new ApiResponse("Found", true, project)).orElseGet(() -> new ApiResponse("Project Not Found", false));
    }

    public ApiResponse delete(UUID id) {
        boolean exists = projectRepository.existsById(id);
        if (!exists) {
            return new ApiResponse("Project Not Found", false);
        }
        projectRepository.deleteById(id);
        return new ApiResponse("Deleted", true);
    }

    public ApiResponse getAllByBranchId(UUID branchId, int page, int size, UUID typeId, UUID stageId, Date startDate, Date endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectList = null;
        Timestamp start = null;
        Timestamp end = null;
        boolean checkingDate = false;
        if (startDate != null && endDate != null) {
            start = new Timestamp(startDate.getTime());
            end = new Timestamp(endDate.getTime());
            checkingDate = true;
        }

        boolean checkingType = false;
        if (typeId != null) {
            checkingType = true;
        }

        boolean checkingStage = false;
        if (stageId != null) {
            checkingStage = true;
        }
        if (page == 0 && size == 0 && !checkingStage && !checkingDate && !checkingType) {
            List<Project> projects = projectRepository.findAllByBranch_Id(branchId);
            if (projects.isEmpty()) {
                return new ApiResponse("Not Found", false);
            }
            return new ApiResponse("Found", true, projects);
        } else if (!checkingStage && !checkingDate && !checkingType) {
            projectList = projectRepository.findAllByBranchId(branchId, pageable);
        } else if (checkingStage && !checkingDate && !checkingType) {
            projectList = projectRepository.findAllByBranchIdAndStageId(branchId, stageId, pageable);
        } else if (checkingStage && checkingDate && !checkingType) {
            projectList = projectRepository.findAllByBranchIdAndStageIdAndCreatedAtBetween(branchId, stageId, start, end, pageable);
        } else if (checkingStage && checkingDate && checkingDate) {
            projectList = projectRepository.findAllByBranchIdAndStageIdAndProjectTypeIdAndCreatedAtBetween(branchId, stageId, typeId, start, end, pageable);
        } else if (checkingStage && checkingType && !checkingDate) {
            projectList = projectRepository.findAllByBranchIdAndStageIdAndProjectTypeId(branchId, stageId, typeId, pageable);
        } else if (checkingDate) {
            projectList = projectRepository.findAllByBranchIdAndCreatedAtBetween(branchId, start, end, pageable);
        }
        assert projectList != null;
        if (projectList.isEmpty()) {
            return new ApiResponse("Project Not Found", false);
        }


        for (Project project : projectList) {
            int completed = taskRepository.countByProjectIdAndTaskStatus_OrginalName(project.getId(), "Completed");
            int all = taskRepository.countByProjectId(project.getId());
            if (completed > 0) {
                int process = 0;
                process = completed * 100 / all;
                project.setProcess(process);
            }
        }
        return new ApiResponse("Found", true, projectList);
    }

    public ApiResponse findByStageId(UUID statusId) {
        List<Project> projects = projectRepository.findAllByStageId(statusId);
        if (projects.isEmpty()) {
            return new ApiResponse("Projects Not Found", false);
        }
        return new ApiResponse("Found", true, projects);
    }


}
