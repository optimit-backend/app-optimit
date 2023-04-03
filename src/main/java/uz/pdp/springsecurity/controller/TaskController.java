package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TaskDto;
import uz.pdp.springsecurity.service.TaskServise;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    TaskServise taskServise;

    @CheckPermission("ADD_TASK")
    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody TaskDto taskDto) {
        ApiResponse apiResponse = taskServise.add(taskDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_TASK")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody TaskDto taskDto) {
        ApiResponse apiResponse = taskServise.edit(id,taskDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_TASK")
    @PatchMapping("/{id}/{statusId}")
    public HttpEntity<?> updateTaskStatus(@PathVariable UUID id,
                                          @PathVariable  UUID statusId) {
        ApiResponse apiResponse = taskServise.updateTaskStatus(id,statusId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_TASK")
    @GetMapping("/{id}")
    public HttpEntity<?> get(@PathVariable UUID id) {
        ApiResponse apiResponse = taskServise.get(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_TASK")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = taskServise.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_ALL_TASK")
    @GetMapping("/get-by-branch/{branchId}")
    public HttpEntity<?> getAllByBusiness(@PathVariable UUID branchId) {
        ApiResponse apiResponse = taskServise.getAllByBranchId(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_ALL_TASK")
    @GetMapping("/get-by-status/{branchId}/{statusId}")
    public HttpEntity<?> getAllByStatus(@PathVariable UUID branchId,
                                        @PathVariable UUID statusId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        ApiResponse apiResponse = taskServise.getAllByStatusId(branchId,statusId,page,size);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
