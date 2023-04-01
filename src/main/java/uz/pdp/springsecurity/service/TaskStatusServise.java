package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Branch;
import uz.pdp.springsecurity.entity.TaskStatus;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TaskStatusDto;
import uz.pdp.springsecurity.repository.BranchRepository;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.TaskStatusRepository;

import java.util.*;

@Service
public class TaskStatusServise {

    @Autowired
    TaskStatusRepository taskStatusRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    BranchRepository branchRepository;

    public ApiResponse add(TaskStatusDto taskStatusDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(taskStatusDto.getBranchId());
        if (optionalBranch.isEmpty()){
            return new ApiResponse("Branch Not Found",false);
        }
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setABoolean(taskStatusDto.isABoolean());
        long ordinalNumber = taskStatusRepository.count()+1;
        taskStatus.setRowNumber(ordinalNumber);
        taskStatus.setName(taskStatusDto.getName());
        taskStatus.setColor(taskStatusDto.getColor());
        taskStatus.setBranch(optionalBranch.get());
        taskStatusRepository.save(taskStatus);
        return new ApiResponse("Added",true,taskStatus);
    }

    public ApiResponse edit(UUID id, TaskStatusDto taskStatusDto) {
        boolean exists = taskStatusRepository.existsById(id);
        if (!exists){
            return new ApiResponse("Not Found",false);
        }
        TaskStatus taskStatus = taskStatusRepository.getById(id);
        taskStatus.setABoolean(taskStatusDto.isABoolean());
        updateTaskStatusOrdinalNumber(taskStatus,taskStatusDto.getRowNumber());
        taskStatus.setName(taskStatusDto.getName());
        taskStatus.setColor(taskStatusDto.getColor());
        TaskStatus status = taskStatusRepository.save(taskStatus);
        return new ApiResponse("Edited",true,status);
    }

    public final void updateTaskStatusOrdinalNumber(TaskStatus taskStatus, long newOrdinalNumber) {
        long currentOrdinalNumber = taskStatus.getRowNumber();
        if (currentOrdinalNumber == newOrdinalNumber) {
            return;
        }

        List<TaskStatus> allTaskStatuses = taskStatusRepository.findAllByOrderByRowNumber();
        if (newOrdinalNumber > currentOrdinalNumber) {
            for (TaskStatus ts : allTaskStatuses) {
                if (ts.getRowNumber() > currentOrdinalNumber && ts.getRowNumber() <= newOrdinalNumber) {
                    ts.setRowNumber(ts.getRowNumber() - 1);
                    taskStatusRepository.save(ts);
                }
            }
        } else {
            for (TaskStatus ts : allTaskStatuses) {
                if (ts.getRowNumber() >= newOrdinalNumber && ts.getRowNumber() < currentOrdinalNumber) {
                    ts.setRowNumber(ts.getRowNumber() + 1);
                    taskStatusRepository.save(ts);
                }
            }
        }
        taskStatus.setRowNumber(newOrdinalNumber);

        taskStatusRepository.save(taskStatus);
    }

    public ApiResponse get(UUID id) {
        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(id);
        return optionalTaskStatus.map(taskStatus -> new ApiResponse("Found", true, taskStatus)).orElseGet(() -> new ApiResponse("Not Found"));
    }

    public ApiResponse delete(UUID id) {
        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(id);
        if (optionalTaskStatus.isPresent()) {
            TaskStatus taskStatusToDelete = optionalTaskStatus.get();

            taskStatusRepository.delete(taskStatusToDelete);


            List<TaskStatus> allTaskStatuses = taskStatusRepository.findAllByOrderByRowNumber();
            int index = 1;
            for (TaskStatus ts : allTaskStatuses) {
                if (ts.getId() != taskStatusToDelete.getId()) {
                    ts.setRowNumber(index++);
                    taskStatusRepository.save(ts);
                }
            }
        }
        return new ApiResponse("Deleted",true);
    }

    public ApiResponse getAllByBranch(UUID branchId) {
        List<String> names = Arrays.asList("Uncompleted","Completed");
        List<TaskStatus> taskStatusList = taskStatusRepository.findAllByNameInOrBranchId(names,branchId);
        if (taskStatusList.isEmpty()){
            return new ApiResponse("Not Found",false);
        }
        taskStatusList.sort(Comparator.comparing(TaskStatus::getRowNumber));
        return new ApiResponse("Found",true,taskStatusList);
    }
}
