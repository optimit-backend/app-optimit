package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.service.WorkTimeService;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/work_time")
@RequiredArgsConstructor
public class WorkTimeController {
    private final WorkTimeService workTimeService;

    @CheckPermission("CREATE_SALARY")
    @PostMapping("/arrive/{userId}")
    public HttpEntity<?> arrive(@PathVariable UUID userId) {
        ApiResponse apiResponse = workTimeService.arrive(userId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_SALARY")
    @PutMapping("/leave/{userId}")
    public HttpEntity<?> leave(@PathVariable UUID userId) {
        ApiResponse apiResponse = workTimeService.leave(userId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_SALARY")
    @GetMapping("/by-user-last-month/{userId}")
    public HttpEntity<?> getAll(@PathVariable UUID userId) {
        ApiResponse apiResponse = workTimeService.getAll(userId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_SALARY")
    @GetMapping("/by-users-on-work/{branchId}")
    public HttpEntity<?> getOnWork(@PathVariable UUID branchId) {
        ApiResponse apiResponse = workTimeService.getOnWork(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
