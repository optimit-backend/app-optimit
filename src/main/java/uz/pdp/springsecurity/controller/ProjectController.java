package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProjectDto;
import uz.pdp.springsecurity.service.ProjectService;

import javax.validation.Valid;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @CheckPermission("ADD_PROJECT")
    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody ProjectDto projectDto) {
        ApiResponse apiResponse = projectService.add(projectDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_PROJECT")
    @PatchMapping("/{projectId}/{statusId}")
    public HttpEntity<?> updateProjectStatus(@PathVariable  UUID projectId,
                                             @PathVariable  UUID statusId) {
        ApiResponse apiResponse = projectService.updateProjectStatus(projectId,statusId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_PROJECT")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody ProjectDto projectDto) {
        ApiResponse apiResponse = projectService.edit(id,projectDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_PROJECT")
    @GetMapping("/{id}")
    public HttpEntity<?> get(@PathVariable UUID id) {
        ApiResponse apiResponse = projectService.get(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("GET_PROJECT")
    @GetMapping("/get-one/{id}")
    public HttpEntity<?> getOne(@PathVariable UUID id) {
        ApiResponse apiResponse = projectService.getOne(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_PROJECT")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = projectService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_PROJECT")
    @GetMapping("/get-by-branch/{branchId}")
    public HttpEntity<?> getAllByBranch(@PathVariable UUID branchId,
                                        @RequestParam(required = false) UUID typeId,
                                        @RequestParam(required = false) UUID customerId,
                                        @RequestParam(required = false) UUID projectStatusId,
                                        @RequestParam(required = false) Date expired,
                                        @RequestParam int page,
                                        @RequestParam int size) {
        ApiResponse apiResponse = projectService.getAllByBranchId(branchId,typeId,customerId,projectStatusId,expired,page,size);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_PROJECT")
    @GetMapping("/get-by-branchId/{branchId}")
    public HttpEntity<?> getAllByBranch(@PathVariable UUID branchId) {
        ApiResponse apiResponse = projectService.getAllByBranch(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
