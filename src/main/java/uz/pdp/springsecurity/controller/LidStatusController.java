package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.entity.LidStatus;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LidFieldDto;
import uz.pdp.springsecurity.payload.LidStatusDto;
import uz.pdp.springsecurity.service.LidStatusService;

import java.util.UUID;

@RestController
@RequestMapping("/lid-status")
@RequiredArgsConstructor
public class LidStatusController {
    private final LidStatusService service;


    @CheckPermission("GET_LID_STATUS")
    @GetMapping("/getByBusinessId/{businessId}")
    public HttpEntity<?> getAll(@PathVariable UUID businessId) {
        ApiResponse apiResponse = service.getAll(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_LID_STATUS")
    @GetMapping("/{id}")
    public HttpEntity<?> getById(@PathVariable UUID id) {
        ApiResponse apiResponse = service.getById(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("POST_LID_STATUS")
    @PostMapping
    public HttpEntity<?> create(@RequestBody LidStatusDto lidStatusDto) {
        ApiResponse apiResponse = service.create(lidStatusDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("PUT_LID_STATUS")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody LidStatusDto lidStatusDto) {
        ApiResponse apiResponse = service.edit(id, lidStatusDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_LID_STATUS")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = service.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
