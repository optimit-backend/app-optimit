package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.TaskStatus;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TaskStatusDto;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.TaskStatusRepository;

import java.util.*;

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
        long ordinalNumber = taskStatusRepository.count()+1;
        taskStatus.setRowNumber(ordinalNumber);
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
        taskStatusList.sort(Comparator.comparing(TaskStatus::getRowNumber));
        return new ApiResponse("Found",true,taskStatusList);
    }
}
