package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TaskDto;
import uz.pdp.springsecurity.service.TaskServise;

import javax.validation.Valid;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskServise taskServise;

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
    @PutMapping("/edit/{id}/{statusId}")
    public HttpEntity<?> updateTaskStatus(@PathVariable UUID id,
                                          @PathVariable  UUID statusId) {
        ApiResponse apiResponse = taskServise.updateTaskStatus(id,statusId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_TASK")
    @PutMapping("change/{statusId}")
    public HttpEntity<?> updateTaskStatus(@PathVariable  UUID statusId,
                                          @RequestParam boolean isIncrease) {
        ApiResponse apiResponse = taskServise.updateTaskStatusIncrease(statusId,isIncrease);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_TASK")
    @GetMapping("/{id}")
    public HttpEntity<?> get(@PathVariable UUID id) {
        ApiResponse apiResponse = taskServise.get(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("GET_TASK")
    @GetMapping("/branch/{branchId}")
    public HttpEntity<?> getAll(@PathVariable UUID branchId) {
        ApiResponse apiResponse = taskServise.getAll(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_TASK")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = taskServise.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_TASK")
    @GetMapping("/get-by-branch/{branchId}")
    public HttpEntity<?> getAllByBranch(@PathVariable UUID branchId,
                                        @RequestParam(required = false) UUID projectId,
                                        @RequestParam(required = false) UUID statusId,
                                        @RequestParam(required = false) UUID typeId,
                                        @RequestParam(required = false) Date expired,
                                        @RequestParam(defaultValue = "0", required = false) int page,
                                        @RequestParam(defaultValue = "10", required = false) int size) {
        ApiResponse apiResponse = taskServise.getAllByBranchId(branchId,projectId,statusId,typeId,expired,page,size);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_TASK")
    @GetMapping("/get-by-project/{projectId}")
    public HttpEntity<?> getAllByProject(@PathVariable UUID projectId) {
        ApiResponse apiResponse = taskServise.getAllByProjectId(projectId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("GET_TASK")
    @GetMapping("/get-by-name/{name}")
    public HttpEntity<?> getAllByName(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @PathVariable String name) {
        ApiResponse apiResponse = taskServise.searchByName(name,page,size);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_TASK")
    @GetMapping("/get-by-branch-pageable/{branchId}")
    public HttpEntity<?> getAllByBranchPageable(@PathVariable UUID branchId,
                                                @RequestParam(required = false) UUID projectId,
                                                @RequestParam(required = false) UUID typeId,
                                                @RequestParam(required = false) Date expired,
                                                @RequestParam(required = false) Map<String,String> params) {
        ApiResponse apiResponse = taskServise.getAllByBranchIdPageable(branchId,params,projectId,typeId,expired);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
