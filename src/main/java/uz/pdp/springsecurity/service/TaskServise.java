package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.Importance;
import uz.pdp.springsecurity.mapper.TaskMapper;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import java.util.*;


@Service
@RequiredArgsConstructor
public class TaskServise {

    @Autowired
    BranchRepository branchRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    TaskTypeRepository taskTypeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StageRepository stageRepository;

    @Autowired
    TaskStatusRepository taskStatusRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ProductionRepository productionRepository;

    private final TaskMapper taskMapper;

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

        List<User> userList = new ArrayList<>();
        for (UUID userId : taskDto.getUsers()) {
            Optional<User> optionalUser = userRepository.findById(userId);
            optionalUser.ifPresent(userList::add);
        }
        task.setUsers(userList);

        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findByOrginalName("Uncompleted");
        optionalTaskStatus.ifPresent(task::setTaskStatus);

        task.setImportance(Importance.valueOf(taskDto.getImportance()));
        if (taskDto.getDependTask() != null) {
            Optional<Task> optionalTask = taskRepository.findById(taskDto.getDependTask());
            optionalTask.ifPresent(task::setDependTask);
        }

        task.setProductions(taskDto.isProduction());

        if (taskDto.getProduction() != null) {
            Optional<Production> optionalProduction = productionRepository.findById(taskDto.getProduction());
            optionalProduction.ifPresent(task::setProduction);
        }
        task.setGoalAmount(taskDto.getGoalAmount());
        task.setTaskPrice(taskDto.getTaskPrice());
        task.setEach(taskDto.isEach());

        task.setBranch(optionalBranch.get());
        taskRepository.save(task);

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
        task.setProductions(taskDto.isProduction());
        if (taskDto.getProduction() != null) {
            Optional<Production> optionalProduction = productionRepository.findById(taskDto.getProduction());
            optionalProduction.ifPresent(task::setProduction);
        }
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
            return new ApiResponse("Not Found", false);
        }
        Task task = optionalTask.get();
        TaskStatus taskStatus = optionalTaskStatus.get();
        if (task.getDependTask() != null) {
            Task depentTask = taskRepository.getById(task.getDependTask().getId());
            if (depentTask.getTaskStatus().getOrginalName() != null && !depentTask.getTaskStatus().getOrginalName().equals("Completed")) {
                return new ApiResponse("You can not change this task, Complete depend task", false);
            }
        }
        task.setTaskStatus(taskStatus);
        taskRepository.save(task);
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

    public ApiResponse getAllByBranchIdPageable(UUID branchId, Map<String, String> params) {

        List<TaskStatus> taskStatusList = taskStatusRepository.findAllByBranchIdOrderByRowNumber(branchId);

        Map<UUID, Integer> value = new HashMap<>();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                value.put(UUID.fromString(entry.getKey()), Integer.valueOf(entry.getValue()));
            }
        }
        List<Map<String, Object>> responses = new ArrayList<>();
        for (TaskStatus status : taskStatusList) {
            Integer integer = null;
            integer = value.get(status.getId());
            Page<Task> allTask = null;

            Pageable pageable = PageRequest.of(0, Objects.requireNonNullElse(integer, 5));

            allTask = taskRepository.findAllByTaskStatusId(status.getId(), pageable);
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


    public ApiResponse getAllByBranchId(UUID branchId, int page, int size) {

        if (page == 0 && size == 0) {
            List<Task> taskList = taskRepository.findAllByBranch_Id(branchId);
            if (taskList.isEmpty()) {
                return new ApiResponse("Not Found", false);
            }
            return new ApiResponse("Found", true, taskList);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> all = taskRepository.findAllByBranchId(branchId, pageable);
        if (all.isEmpty()) {
            return new ApiResponse("Project Not Found", false);
        }

        return new ApiResponse("Found", true, all);
    }
}
