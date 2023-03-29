package uz.pdp.springsecurity.service;

import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TaskStatusDto;

import java.util.UUID;

@Service
public class TaskServise {
    public ApiResponse add(TaskStatusDto taskStatusDto) {
        return null;
    }

    public ApiResponse edit(UUID id, TaskStatusDto taskStatusDto) {
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
