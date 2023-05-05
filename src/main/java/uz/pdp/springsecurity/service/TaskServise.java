package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.GeoPage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.Importance;
import uz.pdp.springsecurity.enums.NotificationType;
import uz.pdp.springsecurity.mapper.TaskMapper;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import java.sql.Timestamp;
import java.util.*;


@Service
@RequiredArgsConstructor
public class TaskServise {

    private final BranchRepository branchRepository;
    private final ProjectRepository projectRepository;
    private final TaskTypeRepository taskTypeRepository;
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskRepository taskRepository;
    private final TaskPriceRepository taskPriceRepository;
    private final TaskMapper taskMapper;
    private final NotificationRepository notificationRepository;
    private final ContentRepository contentRepository;
    private final StageRepository stageRepository;
    private final SalaryCountService salaryCountService;
    private final PrizeService prizeService;

    public ApiResponse add(TaskDto taskDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(taskDto.getBranchId());
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }
        Task task = new Task();
        task.setName(taskDto.getName());

        Optional<TaskType> optionalTaskType = taskTypeRepository.findById(taskDto.getTaskTypeId());
        optionalTaskType.ifPresent(task::setTaskType);
        if (taskDto.getProjectId() != null) {
            Optional<Project> optionalProject = projectRepository.findById(taskDto.getProjectId());
            optionalProject.ifPresent(task::setProject);
        }
        task.setStartDate(taskDto.getStartDate());
        if (taskDto.getDeadLine() != null) {
            task.setDeadLine(taskDto.getDeadLine());
        }
        if (taskDto.getContentId() != null) {
            Optional<Content> optionalContent = contentRepository.findById(taskDto.getContentId());
            if (optionalContent.isPresent()) {
                Content content = optionalContent.get();
                task.setContent(content);
            }
        }
        List<TaskPrice> taskPriceList = new ArrayList<>();
        for (TaskPriceDto taskPriceDto : taskDto.getTaskPriceDtos()) {
            TaskPrice taskPrice = new TaskPrice();
            taskPrice.setPrice(taskPriceDto.getPrice());
            List<User> userList = userRepository.findAllByIdIn(taskPriceDto.getUserList());
            taskPrice.setUserList(userList);
            taskPrice.setEach(taskPriceDto.isEach());
            taskPriceList.add(taskPrice);
        }
        taskPriceRepository.saveAll(taskPriceList);
        task.setTaskPriceList(taskPriceList);

        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findByBranchIdAndOrginalName(taskDto.getBranchId(), "Uncompleted");
        optionalTaskStatus.ifPresent(task::setTaskStatus);

        task.setImportance(Importance.valueOf(taskDto.getImportance()));
        if (taskDto.getDependTask() != null) {
            Optional<Task> optionalTask = taskRepository.findById(taskDto.getDependTask());
            optionalTask.ifPresent(task::setDependTask);
        }

        task.setProductions(taskDto.isProductions());

        task.setGoalAmount(taskDto.getGoalAmount());
        task.setTaskPrice(taskDto.getTaskPrice());

        if (taskDto.getStageId() != null) {
            Optional<Stage> optionalStage = stageRepository.findById(taskDto.getStageId());
            if (optionalStage.isEmpty()) {
                return new ApiResponse("Stage not found", false);
            }
            Stage stage = optionalStage.get();
            task.setStage(stage);
        }

        task.setBranch(optionalBranch.get());
        Project project = null;
        if (taskDto.getProjectId() != null) {
            Optional<Project> optionalProject = projectRepository.findById(taskDto.getProjectId());
            if (optionalProject.isPresent()) {
                project = optionalProject.get();
            }
            assert project != null;
            double budget = project.getBudget();
            double taskPrice = task.getTaskPrice();
            budget = budget - taskPrice;
            project.setBudget(budget);
            projectRepository.save(project);
        }
        taskRepository.save(task);

        List<User> users = new ArrayList<>();
        for (TaskPrice taskPrice : taskPriceList) {
            users.addAll(taskPrice.getUserList());
        }
        for (User user : users) {
            Notification notification = new Notification();
            notification.setRead(false);
            notification.setName("You have been given a new task!");
            notification.setMessage("Your assignment is at this link!");
            notification.setType(NotificationType.NEW_TASK);
            notification.setObjectId(task.getId());
            notification.setUserTo(user);
            notificationRepository.save(notification);
        }

        return new ApiResponse("Added", true);
    }

    public ApiResponse edit(UUID id, TaskDto taskDto) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) {
            return new ApiResponse("Task not found", false);
        }
        Task task = optionalTask.get();
        task.setName(taskDto.getName());
        if (taskDto.getTaskTypeId() != null) {
            Optional<TaskType> optionalTaskType = taskTypeRepository.findById(taskDto.getTaskTypeId());
            optionalTaskType.ifPresent(task::setTaskType);
        }
        if (taskDto.getProjectId() != null) {
            Optional<Project> optionalProject = projectRepository.findById(taskDto.getProjectId());
            optionalProject.ifPresent(task::setProject);
        }
        if (taskDto.getStageId() != null) {
            Optional<Stage> optionalStage = stageRepository.findById(taskDto.getStageId());
            optionalStage.ifPresent(task::setStage);
        }

        task.setStartDate(taskDto.getStartDate());
        task.setDeadLine(taskDto.getDeadLine());
        List<TaskPrice> taskPriceList = new ArrayList<>();
        for (TaskPriceDto taskPriceDto : taskDto.getTaskPriceDtos()) {
            TaskPrice taskPrice = taskPriceRepository.getById(taskPriceDto.getId());
            taskPrice.setPrice(taskPriceDto.getPrice());
            taskPrice.setEach(taskPriceDto.isEach());
            List<User> users = userRepository.findAllByIdIn(taskPriceDto.getUserList());
            taskPrice.setUserList(users);
            taskPriceList.add(taskPrice);
        }
        task.setTaskPriceList(taskPriceList);
        taskPriceRepository.saveAll(taskPriceList);
        if (taskDto.getTaskStatus() != null) {
            Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(taskDto.getTaskStatus());
            optionalTaskStatus.ifPresent(task::setTaskStatus);
        }
        task.setImportance(Importance.valueOf(taskDto.getImportance()));
        if (taskDto.getDependTask() != null) {
            Optional<Task> taskOptional = taskRepository.findById(taskDto.getDependTask());
            taskOptional.ifPresent(task::setDependTask);
        }
        task.setProductions(taskDto.isProductions());
        task.setGoalAmount(taskDto.getGoalAmount());
        task.setTaskPrice(taskDto.getTaskPrice());
        taskRepository.save(task);
        return new ApiResponse("Edited", true);
    }

    public ApiResponse updateTaskStatus(UUID id, UUID taskStatusId) {

        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(taskStatusId);
        if (optionalTaskStatus.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        Task task = optionalTask.get();
        TaskStatus taskStatus = optionalTaskStatus.get();
        if (taskStatus.getOrginalName() != null) {
            if (task.getDependTask() != null && taskStatus.getOrginalName().equals("Completed")) {
                Task depentTask = taskRepository.getById(task.getDependTask().getId());
                if (depentTask.getTaskStatus().getOrginalName() != null && !depentTask.getTaskStatus().getOrginalName().equals("Completed")) {
                    return new ApiResponse("You can not change this task, Complete " + depentTask.getName() + " task", false);
                }
            }
        }
        if (task.getTaskStatus().getOrginalName() != null && task.getTaskStatus().getOrginalName().equals("Completed")) {
            return new ApiResponse("You can not change this task !", false);
        }
        if (taskStatus.getOrginalName() != null && taskStatus.getOrginalName().equals("Completed")) {
            Date deadline = task.getDeadLine();
            Date endDate = new Date();
            task.setExpired(!deadline.after(endDate));
            task.setEndDate(endDate);
            salaryCountService.addForTask(task);
            prizeService.addForTask(task);
        }
        task.setTaskStatus(taskStatus);
        taskRepository.save(task);
        return new ApiResponse("Edited", true);
    }

    public ApiResponse updateTaskStatusIncrease(UUID taskStatusId, boolean isIncrease) {
        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(taskStatusId);
        if (optionalTaskStatus.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        TaskStatus taskStatus = optionalTaskStatus.get();
        taskStatus.setABoolean(isIncrease);
        taskStatusRepository.save(taskStatus);
        return new ApiResponse("Edited", true);
    }

    public ApiResponse get(UUID id) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        return optionalTask.map(task -> new ApiResponse("Found", true, task)).orElseGet(() -> new ApiResponse("Not Found", false));
    }

    public ApiResponse delete(UUID id) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) {
            return new ApiResponse("Task Not Found", false);
        }
        taskRepository.deleteById(id);
        return new ApiResponse("Deleted", true);
    }

    public ApiResponse getAllByBranchIdPageable(UUID branchId, Map<String, String> params, UUID projectId, UUID typeId, UUID userId, Date expired) {

        boolean checkingProject = false;
        boolean checkingType = false;
        boolean checkingExpired = false;
        if (projectId != null) {
            checkingProject = true;
        }
        if (typeId != null) {
            checkingType = true;
        }
        if (expired != null) {
            checkingExpired = true;
        }
        List<TaskStatus> taskStatusList = taskStatusRepository.findAllByBranchIdOrderByRowNumber(branchId);

        Map<UUID, Integer> value = new HashMap<>();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                try {
                    value.put(UUID.fromString(entry.getKey()), Integer.valueOf(entry.getValue()));
                } catch (Exception e) {
                    continue;
                }
            }
        }
        List<Map<String, Object>> responses = new ArrayList<>();
        for (TaskStatus status : taskStatusList) {
            Integer integer = null;
            integer = value.get(status.getId());
            Page<Task> allTask = null;

            Pageable pageable = PageRequest.of(0, Objects.requireNonNullElse(integer, 5));

            if (userId == null) {
                if (checkingProject && checkingType && checkingExpired) {
                    allTask = taskRepository.findAllByTaskStatusIdAndProjectIdAndTaskTypeIdAndExpiredTrue(status.getId(), projectId, typeId, pageable);
                } else if (checkingProject && checkingType) {
                    allTask = taskRepository.findAllByTaskStatusIdAndProjectIdAndTaskTypeId(status.getId(), projectId, typeId, pageable);
                } else if (checkingProject && checkingExpired) {
                    allTask = taskRepository.findAllByTaskStatusIdAndProjectIdAndExpiredTrue(status.getId(), projectId, pageable);
                } else if (checkingType && checkingExpired) {
                    allTask = taskRepository.findAllByTaskStatusIdAndTaskTypeIdAndExpiredTrue(status.getId(), typeId, pageable);
                } else if (checkingProject) {
                    allTask = taskRepository.findAllByTaskStatusIdAndProject_Id(status.getId(), projectId, pageable);
                } else if (checkingType) {
                    allTask = taskRepository.findAllByTaskStatusIdAndTaskTypeId(status.getId(), typeId, pageable);
                } else if (checkingExpired) {
                    allTask = taskRepository.findAllByTaskStatusIdAndExpiredTrue(status.getId(), pageable);
                } else {
                    allTask = taskRepository.findAllByTaskStatus_Id(status.getId(), pageable);
                }
            } else {
                if (checkingProject && checkingType && checkingExpired) {
                    allTask = taskRepository.findAllByTaskStatusIdAndProjectIdAndTaskTypeIdAndExpiredTrueAndTaskPriceList_UserList_Id(status.getId(), projectId, typeId, userId, pageable);
                } else if (checkingProject && checkingType) {
                    allTask = taskRepository.findAllByTaskStatusIdAndProjectIdAndTaskTypeIdAndTaskPriceList_UserList_Id(status.getId(), projectId, typeId, userId, pageable);
                } else if (checkingProject && checkingExpired) {
                    allTask = taskRepository.findAllByTaskStatusIdAndProjectIdAndExpiredTrueAndTaskPriceList_UserList_Id(status.getId(), projectId, userId, pageable);
                } else if (checkingType && checkingExpired) {
                    allTask = taskRepository.findAllByTaskStatusIdAndTaskTypeIdAndExpiredTrueAndTaskPriceList_UserList_Id(status.getId(), typeId, userId, pageable);
                } else if (checkingProject) {
                    allTask = taskRepository.findAllByTaskStatusIdAndProject_IdAndTaskPriceList_UserList_Id(status.getId(), projectId, userId, pageable);
                } else if (checkingType) {
                    allTask = taskRepository.findAllByTaskStatusIdAndTaskTypeIdAndTaskPriceList_UserList_Id(status.getId(), typeId, userId, pageable);
                } else if (checkingExpired) {
                    allTask = taskRepository.findAllByTaskStatusIdAndExpiredTrueAndTaskPriceList_UserList_Id(status.getId(), userId, pageable);
                } else {
                    allTask = taskRepository.findAllByTaskStatus_IdAndTaskPriceList_UserList_Id(status.getId(), userId, pageable);
                }
            }

            List<TaskGetDto> taskGetDtoList = taskMapper.toDto(allTask.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("statusId", status.getId());
            response.put("getLessProduct", taskGetDtoList);
            response.put("currentPage", allTask.getNumber());
            response.put("totalItems", allTask.getTotalElements());
            response.put("totalPages", allTask.getTotalPages());

            responses.add(response);
        }

        return new ApiResponse("Found", true, responses);
    }

    public ApiResponse getAllByProjectId(UUID projectId) {

        List<Task> taskList = taskRepository.findAllByProjectId(projectId);
        if (taskList.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        return new ApiResponse("Found", true, taskList);
    }

    public ApiResponse getAllByBranchId(UUID branchId, UUID projectId, UUID statusId, UUID typeId, UUID userId, Date expired, int page, int size) {

        Page<Task> tasks = null;
        Pageable pageable = PageRequest.of(page, size);
        if (userId != null) {
            tasks = taskRepository.findAllByTaskPriceList_UserList_Id(userId, pageable);
        } else if (projectId != null && statusId != null && typeId != null) {
            tasks = taskRepository.findAllByProjectIdAndTaskStatusIdAndTaskTypeIdAndExpiredTrue(projectId, statusId, typeId, pageable);
        } else if (statusId != null && typeId != null) {
            tasks = taskRepository.findAllByTaskStatusIdAndTaskTypeIdAndExpiredTrue(statusId, typeId, pageable);
        } else if (typeId != null && expired != null) {
            tasks = taskRepository.findAllByProjectIdAndTaskTypeIdAndExpiredTrue(projectId, typeId, pageable);
        } else if (projectId != null && expired != null) {
            tasks = taskRepository.findAllByProjectIdAndExpiredTrue(projectId, pageable);
        } else if (statusId != null && expired != null) {
            tasks = taskRepository.findAllByTaskStatusIdAndExpiredTrue(statusId, pageable);
        } else if (typeId != null) {
            tasks = taskRepository.findAllByTaskTypeId(typeId, pageable);
        } else if (projectId != null) {
            tasks = taskRepository.findAllByProject_Id(projectId, pageable);
        } else if (expired != null) {
            tasks = taskRepository.findAllByBranch_IdAndExpiredTrue(branchId, pageable);
        } else if (statusId != null) {
            tasks = taskRepository.findAllByTaskStatusId(statusId, pageable);
        } else {
            tasks = taskRepository.findAllByBranch_Id(branchId, pageable);
        }
        if (Objects.requireNonNull(tasks).isEmpty()) {
            return new ApiResponse("Not Found", false);
        }

        return new ApiResponse("Found", true, tasks);
    }

    public ApiResponse getAll(UUID branchId, UUID userId) {
        List<Task> taskList = null;
        if (userId != null) {
            taskList = taskRepository.findTasksByUserId(userId);
        } else {
            taskList = taskRepository.findAllByBranchId(branchId);
        }
        if (taskList.isEmpty()) {
            return new ApiResponse("Tasks not found", false);
        }
        return new ApiResponse("Found", true, taskList);
    }

    public ApiResponse searchByName(String name, int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);
        String[] words = name.split("\\s+");
        Page<Task> taskPage = null;
        if (userId == null) {
            for (String word : words) {
                taskPage = taskRepository.findByNameContainingIgnoreCase(word, pageable);
            }
        } else {
            for (String word : words) {
                taskPage = taskRepository.findByNameContainingIgnoreCaseAndTaskPriceList_UserList_Id(word, userId, pageable);
            }
        }
        if (taskPage == null) {
            return new ApiResponse("Not Found", false);
        }
        return new ApiResponse("Found", true, taskPage);
    }

    public ApiResponse getOwnTask(UUID userId, UUID branchId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }

        Page<Task> tasks = null;

        if (userId != null) {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return new ApiResponse("User Not Found");
            }
            tasks = taskRepository.findAllByBranch_IdAndTaskPriceList_UserList_Id(branchId, userId, pageable);
        } else {
            tasks = taskRepository.findAllByBranch_Id(branchId, pageable);
        }
        
        return new ApiResponse("Found", true, tasks);
    }
}
