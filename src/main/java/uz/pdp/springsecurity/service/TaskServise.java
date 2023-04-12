package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final ProductionRepository productionRepository;
    private final TaskMapper taskMapper;
    private final NotificationRepository notificationRepository;
    private final ContentRepository contentRepository;

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
        task.setEndDate(taskDto.getEndDate());
        if (taskDto.getDeadLine() != null){
            task.setDeadLine(taskDto.getDeadLine());
        }
        if (taskDto.getContentId()!=null){
            Optional<Content> optionalContent = contentRepository.findById(taskDto.getContentId());
            if (optionalContent.isPresent()){
                Content content = optionalContent.get();
                task.setContent(content);
            }
        }

        List<User> userList = new ArrayList<>();
        for (UUID userId : taskDto.getUsers()) {
            Optional<User> optionalUser = userRepository.findById(userId);
            optionalUser.ifPresent(userList::add);
        }
        task.setUsers(userList);

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
        task.setEach(taskDto.isEach());

        task.setBranch(optionalBranch.get());
        Project project = null;
        if (taskDto.getProjectId() != null){
            Optional<Project> optionalProject = projectRepository.findById(taskDto.getProjectId());
            if (optionalProject.isPresent()){
                project = optionalProject.get();
            }
            double budget = project.getBudget();
            double taskPrice = task.getTaskPrice();
            int size = userList.size();
            if (taskDto.isEach()){
                budget =  budget-(taskPrice*size);
                project.setBudget(budget);
                projectRepository.save(project);
            }else {
                budget = budget - (taskPrice);
                project.setBudget(budget);
                projectRepository.save(project);
            }
        }

        taskRepository.save(task);



        List<User> users = task.getUsers();
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
        task.setStartDate(taskDto.getStartDate());
        task.setEndDate(taskDto.getEndDate());
        task.setDeadLine(taskDto.getDeadLine());
        if (taskDto.getUsers() != null) {
            List<User> userList = new ArrayList<>();
            for (UUID user : taskDto.getUsers()) {
                Optional<User> optionalUser = userRepository.findById(user);
                optionalUser.ifPresent(userList::add);
            }
            task.setUsers(userList);
        }
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
        task.setEach(taskDto.isEach());
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
            TaskStatus taskStatus = optionalTaskStatus.get();
            return new ApiResponse("Not Found", false);
        }
        Task task = optionalTask.get();
        TaskStatus taskStatus = optionalTaskStatus.get();
        if (task.getDependTask() != null && taskStatus.getOrginalName().equals("Completed")) {
            Task depentTask = taskRepository.getById(task.getDependTask().getId());
            if (depentTask.getTaskStatus().getOrginalName() != null && !depentTask.getTaskStatus().getOrginalName().equals("Completed")) {
                return new ApiResponse("You can not change this task, Complete " + depentTask.getName() + " task", false);
            }
        }
        if (task.getTaskStatus().getName().equals("Completed")){
            return new ApiResponse("You can not change this task !", false);
        }
        if (taskStatus.getOrginalName() != null && taskStatus.getOrginalName().equals("Completed")){
            task.setEndDate(new Date());
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

    public ApiResponse getAllByBranchIdPageable(UUID branchId, Map<String, String> params, UUID projectId, UUID typeId, Date startDate, Date endDate) {

        Timestamp start = null;
        Timestamp end = null;
        boolean checkingDate = false;
        boolean checkingProject = false;
        boolean checkingType = false;
        if (projectId != null) {
            checkingProject = true;
        }
        if (typeId != null) {
            checkingType = true;
        }
        if (startDate != null && endDate != null) {
            checkingDate = true;
            start = new Timestamp(startDate.getTime());
            end = new Timestamp(endDate.getTime());
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

            if (checkingProject && checkingType && checkingDate) {
                allTask = taskRepository.findAllByTaskStatusIdAndProjectIdAndTaskTypeIdAndCreatedAtBetween(status.getId(), projectId, typeId, start, end, pageable);
            } else if (checkingProject && checkingType) {
                allTask = taskRepository.findAllByTaskStatusIdAndProjectIdAndTaskTypeId(status.getId(), projectId, typeId, pageable);
            } else if (checkingProject && checkingDate) {
                allTask = taskRepository.findAllByTaskStatusIdAndProjectIdAndCreatedAtBetween(status.getId(), projectId, start, end, pageable);
            } else if (checkingProject) {
                allTask = taskRepository.findAllByTaskStatusIdAndProjectId(status.getId(), projectId, pageable);
            } else if (checkingType && checkingDate) {
                allTask = taskRepository.findAllByTaskStatusIdAndTaskTypeIdAndCreatedAtBetween(status.getId(), typeId, start, end, pageable);
            } else if (checkingType) {
                allTask = taskRepository.findAllByTaskStatusIdAndTaskTypeId(status.getId(), typeId, pageable);
            } else if (checkingDate) {
                allTask = taskRepository.findAllByTaskStatusIdAndCreatedAtBetween(status.getId(), start, end, pageable);
            } else {
                allTask = taskRepository.findAllByTaskStatus_Id(status.getId(), pageable);
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

    public ApiResponse getAllByBranchId(UUID branchId, UUID projectId, UUID statusId, UUID typeId, Date startDate, Date endDate, int page, int size) {

        Page<Task> tasks = null;
        Pageable pageable = PageRequest.of(page, size);

        Timestamp start = null;
        Timestamp end = null;

        if (startDate != null && endDate != null) {
            start = new Timestamp(startDate.getTime());
            end = new Timestamp(endDate.getTime());

        }
        if (typeId == null && projectId == null && statusId == null && startDate == null && endDate == null) {
            tasks = taskRepository.findAllByBranchId(branchId, pageable);
        } else if (projectId != null && statusId == null && startDate == null && endDate == null) {
            tasks = taskRepository.findAllByProject_Id(projectId, pageable);
        } else if (projectId == null && statusId != null && startDate == null && endDate == null) {
            tasks = taskRepository.findAllByTaskStatus_Id(statusId, pageable);
        } else if (projectId == null && statusId == null && startDate != null && endDate != null) {
            tasks = taskRepository.findAllByBranchIdAndCreatedAtBetween(branchId, start, end, pageable);
        } else if (projectId != null && statusId == null && startDate != null && endDate != null) {
            tasks = taskRepository.findAllByBranchIdAndProject_IdAndCreatedAtBetween(branchId, projectId, start, end, pageable);
        } else if (projectId != null && statusId != null && startDate != null && endDate != null && typeId != null) {
            tasks = taskRepository.findAllByBranchIdAndProject_IdAndTaskTypeIdAndTaskStatus_IdAndCreatedAtBetween(branchId, projectId, typeId, statusId, start, end, pageable);
        } else if (projectId == null && statusId != null && startDate != null && endDate != null && typeId != null) {
            tasks = taskRepository.findAllByBranchIdAndTaskTypeIdAndTaskStatus_IdAndCreatedAtBetween(branchId, typeId, statusId, start, end, pageable);
        } else if (projectId != null && statusId != null && startDate == null && endDate == null && typeId == null) {
            tasks = taskRepository.findAllByBranchIdAndTaskStatus_Id(branchId, statusId, pageable);
        } else if (projectId != null && statusId != null && startDate != null && endDate != null) {
            tasks = taskRepository.findAllByBranchIdAndProjectIdAndTaskStatus_IdAndCreatedAtBetween(branchId, projectId, statusId, start, end, pageable);
        } else if (projectId == null && statusId != null && startDate != null && endDate != null && typeId == null) {
            tasks = taskRepository.findAllByBranchIdAndTaskStatus_IdAndCreatedAtBetween(branchId, statusId, start, end, pageable);
        }
        if (Objects.requireNonNull(tasks).isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        return new ApiResponse("Found", true, tasks);
    }
}
