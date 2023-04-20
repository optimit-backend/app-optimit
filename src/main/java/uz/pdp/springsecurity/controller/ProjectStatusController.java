package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProjectStatusDto;
import uz.pdp.springsecurity.service.ProjectStatusServise;

import javax.validation.Valid;
import java.util.UUID;
@RestController
@RequestMapping("/api/project/status")
@RequiredArgsConstructor
public class ProjectStatusController {

    @Autowired
    ProjectStatusServise projectStatusServise;

    @CheckPermission("ADD_PROJECT_STATUS")
    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody ProjectStatusDto projectStatusDto) {
        ApiResponse apiResponse = projectStatusServise.add(projectStatusDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_PROJECT_STATUS")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody ProjectStatusDto projectStatusDto) {
        ApiResponse apiResponse = projectStatusServise.edit(id,projectStatusDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_PROJECT_STATUS")
    @GetMapping("/{id}")
    public HttpEntity<?> get(@PathVariable UUID id) {
        ApiResponse apiResponse = projectStatusServise.get(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_PROJECT_STATUS")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = projectStatusServise.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_ALL_PROJECT_STATUS")
    @GetMapping("/get-by-branch/{branchId}")
    public HttpEntity<?> getAllByBranch(@PathVariable UUID branchId) {
        ApiResponse apiResponse = projectStatusServise.getAllByBranch(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
