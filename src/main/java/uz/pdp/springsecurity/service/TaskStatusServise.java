package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.TaskStatus;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TaskStatusDto;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.TaskStatusRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskStatusServise {

    @Autowired
    TaskStatusRepository taskStatusRepository;

    @Autowired
    BusinessRepository businessRepository;
    public ApiResponse add(TaskStatusDto taskStatusDto) {
        Optional<Business> optionalBusiness = businessRepository.findById(taskStatusDto.getBusinessId());
        if (optionalBusiness.isEmpty()){
            return new ApiResponse("Business Not Found",false);
        }
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setABoolean(taskStatusDto.isABoolean());
        taskStatus.setOrdinalNumber(taskStatusDto.getOrdinalNumber());
        taskStatus.setName(taskStatusDto.getName());
        taskStatus.setColor(taskStatusDto.getColor());
        taskStatus.setBusiness(optionalBusiness.get());
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
        updateTaskStatusOrdinalNumber(taskStatus,taskStatusDto.getOrdinalNumber());
        taskStatus.setName(taskStatusDto.getName());
        taskStatus.setColor(taskStatusDto.getColor());
        TaskStatus status = taskStatusRepository.save(taskStatus);
        return new ApiResponse("Edited",true,status);
    }

    public final void updateTaskStatusOrdinalNumber(TaskStatus taskStatus, int newOrdinalNumber) {
        int currentOrdinalNumber = taskStatus.getOrdinalNumber();
        if (currentOrdinalNumber == newOrdinalNumber) {
            return; // No need to update if the ordinal numbers are the same
        }

        List<TaskStatus> allTaskStatuses = taskStatusRepository.findAllByOrderByOrdinalNumber();
        if (newOrdinalNumber > currentOrdinalNumber) {
            // Move the task status to a higher ordinal number
            for (TaskStatus ts : allTaskStatuses) {
                if (ts.getOrdinalNumber() > currentOrdinalNumber && ts.getOrdinalNumber() <= newOrdinalNumber) {
                    ts.setOrdinalNumber(ts.getOrdinalNumber() - 1);
                    taskStatusRepository.save(ts);
                }
            }
        } else {
            // Move the task status to a lower ordinal number
            for (TaskStatus ts : allTaskStatuses) {
                if (ts.getOrdinalNumber() >= newOrdinalNumber && ts.getOrdinalNumber() < currentOrdinalNumber) {
                    ts.setOrdinalNumber(ts.getOrdinalNumber() + 1);
                    taskStatusRepository.save(ts);
                }
            }
        }
        taskStatus.setOrdinalNumber(newOrdinalNumber);

        taskStatusRepository.save(taskStatus);
    }

    public ApiResponse get(UUID id) {
        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(id);
        return optionalTaskStatus.map(taskStatus -> new ApiResponse("Found", true, taskStatus)).orElseGet(() -> new ApiResponse("Not Found"));
    }

    public ApiResponse delete(UUID id) {
        boolean exists = taskStatusRepository.existsById(id);
        if (!exists){
            return new ApiResponse("Not Found",false);
        }
        taskStatusRepository.deleteById(id);
        return new ApiResponse("Deleted",true);
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        List<TaskStatus> taskStatusList = taskStatusRepository.findAllByBusiness_Id(businessId);
        if (taskStatusList.isEmpty()){
            return new ApiResponse("Not Found",false);
        }
        return new ApiResponse("Found",true,taskStatusList);
    }
}
