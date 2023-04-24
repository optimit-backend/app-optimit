package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.WorkTimePostDto;
import uz.pdp.springsecurity.service.WorkTimeService;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/workTime")
@RequiredArgsConstructor
public class WorkTimeController {
    private final WorkTimeService workTimeService;

    @CheckPermission("ADD_WORK_TIME")
    @PostMapping("/arrive")
    public HttpEntity<?> arrive(@RequestBody WorkTimePostDto workTimePostDto) {
        ApiResponse apiResponse = workTimeService.arrive(workTimePostDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("ADD_WORK_TIME")
    @PutMapping("/leave")
    public HttpEntity<?> leave(@RequestBody WorkTimePostDto workTimePostDto) {
        ApiResponse apiResponse = workTimeService.leave(workTimePostDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_WORK_TIME")
    @GetMapping("/by-user-last-month/{userId}")
    public HttpEntity<?> getByUserLastMonth(@PathVariable UUID userId, @RequestParam() UUID branchId) {
        ApiResponse apiResponse = workTimeService.getByUserLastMonth(userId, branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_WORK_TIME")
    @GetMapping("/by-users-on-work/{branchId}")
    public HttpEntity<?> getOnWork(@PathVariable UUID branchId) {
        ApiResponse apiResponse = workTimeService.getOnWork(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_WORK_TIME")
    @GetMapping("/by-branch-come-work/{branchId}")
    public HttpEntity<?> getComeWork(@PathVariable UUID branchId) {
        ApiResponse apiResponse = workTimeService.getComeWork(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
