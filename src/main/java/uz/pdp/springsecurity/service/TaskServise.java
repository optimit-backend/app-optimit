package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.Importance;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TaskDto;
import uz.pdp.springsecurity.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;



@Service
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

    public ApiResponse add(TaskDto taskDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(taskDto.getBranchId());
        if (optionalBranch.isEmpty()){
            return new ApiResponse("Branch Not Found",false);
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
        if (taskDto.getDependTask() != null){
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

        return new ApiResponse("Added",true);
    }

    public ApiResponse edit(UUID id, TaskDto taskDto) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()){
            return new ApiResponse("Task not found",false);
        }
        Task task = optionalTask.get();
        task.setName(taskDto.getName());
        if (taskDto.getTaskTypeId() != null){
            Optional<TaskType> optionalTaskType = taskTypeRepository.findById(taskDto.getTaskTypeId());
            optionalTaskType.ifPresent(task::setTaskType);
        }
        if (taskDto.getProjectId() != null){
            Optional<Project> optionalProject = projectRepository.findById(taskDto.getProjectId());
            optionalProject.ifPresent(task::setProject);
        }
        task.setStartDate(taskDto.getStartDate());
        task.setEndDate(taskDto.getEndDate());
        if (taskDto.getUsers() != null){
            List<User> userList = new ArrayList<>();
            for (UUID user : taskDto.getUsers()) {
                Optional<User> optionalUser = userRepository.findById(user);
                optionalUser.ifPresent(userList::add);
            }
            task.setUsers(userList);
        }
        if (taskDto.getTaskStatus() != null){
            Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(taskDto.getTaskStatus());
            optionalTaskStatus.ifPresent(task::setTaskStatus);
        }
        task.setImportance(Importance.valueOf(taskDto.getImportance()));
        if (taskDto.getDependTask() != null){
            Optional<Task> taskOptional = taskRepository.findById(taskDto.getDependTask());
            taskOptional.ifPresent(task::setDependTask);
        }
        task.setProductions(taskDto.isProduction());
        if (taskDto.getProduction() != null){
            Optional<Production> optionalProduction = productionRepository.findById(taskDto.getProduction());
            optionalProduction.ifPresent(task::setProduction);
        }
        task.setGoalAmount(taskDto.getGoalAmount());
        task.setTaskPrice(taskDto.getTaskPrice());
        task.setEach(taskDto.isEach());
        taskRepository.save(task);
        return new ApiResponse("Edited",true);
    }

    public ApiResponse updateTaskStatus(UUID id, UUID taskStatusId) {

        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()){
            return new ApiResponse("Not Found",false);
        }
        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(taskStatusId);
        if (optionalTaskStatus.isEmpty()){
            return new ApiResponse("Not Found",false);
        }
        Task task = optionalTask.get();
        TaskStatus taskStatus = optionalTaskStatus.get();
        task.setTaskStatus(taskStatus);
        taskRepository.save(task);
        return new ApiResponse("Edited",true);
    }

    public ApiResponse get(UUID id) {
        return null;
    }

    public ApiResponse delete(UUID id) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()){
            return new ApiResponse("Task Not Found",false);
        }
        taskRepository.deleteById(id);
        return new ApiResponse("Deleted",true);
    }

    public ApiResponse getAllByBranchId(UUID branchId) {
        return null;
    }

    public ApiResponse getAllByStatusId(UUID branchId,UUID statusId,int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Task> all = taskRepository.findAllByBranchIdAndTaskStatusId(branchId, statusId, pageable);
        if (all.isEmpty()){
            assert all.getTotalElements() <= 0 : "list is empty";
            return new ApiResponse("Project Not Found",false);
        }
        return new ApiResponse("Found",true,all);
    }
}
