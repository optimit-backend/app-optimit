package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.Importance;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TaskDto;
import uz.pdp.springsecurity.payload.TaskStatusDto;
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

        Optional<Project> optionalProject = projectRepository.findById(taskDto.getProjectId());
        optionalProject.ifPresent(task::setProject);

        task.setStartDate(taskDto.getStartDate());
        task.setEndDate(taskDto.getEndDate());

        List<User> userList = new ArrayList<>();
        for (UUID userId : taskDto.getUsers()) {
            Optional<User> optionalUser = userRepository.findById(userId);
            optionalUser.ifPresent(userList::add);
        }
        task.setUsers(userList);

        Optional<Stage> optionalStage = stageRepository.findById(taskDto.getStage());
        optionalStage.ifPresent(task::setStage);

        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(taskDto.getTaskStatus());
        optionalTaskStatus.ifPresent(task::setTaskStatus);

        task.setImportance(Importance.valueOf(taskDto.getImportance()));

        Optional<Task> optionalTask = taskRepository.findById(taskDto.getDependTask());
        optionalTask.ifPresent(task::setDependTask);

        task.setProductions(taskDto.isProduction());

        Optional<Production> optionalProduction = productionRepository.findById(taskDto.getProduction());
        optionalProduction.ifPresent(task::setProduction);

        task.setGoalAmount(taskDto.getGoalAmount());
        task.setTaskPrice(taskDto.getTaskPrice());
        task.setEach(taskDto.isEach());

        task.setBranch(optionalBranch.get());
        taskRepository.save(task);

        return new ApiResponse("Added",true);
    }

    public ApiResponse edit(UUID id, TaskDto taskDto) {
        return null;
    }

    public ApiResponse get(UUID id) {
        return null;
    }

    public ApiResponse delete(UUID id) {
        return null;
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        return null;
    }
}
