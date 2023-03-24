package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.SalaryDto;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.service.SalaryService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/salary")
@RequiredArgsConstructor
public class SalaryController {
    private final SalaryService salaryService;

    @CheckPermission("CREATE_SALARY")
    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody SalaryDto salaryDto) {
        ApiResponse apiResponse = salaryService.add(salaryDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_SALARY")
    @PutMapping("/{salaryId}")
    public HttpEntity<?> edit(@PathVariable UUID salaryId, @Valid @RequestBody SalaryDto salaryDto) {
        ApiResponse apiResponse = salaryService.edit(salaryId, salaryDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_SALARY")
    @GetMapping("/by-branch/{branchId}")
    public HttpEntity<?> getAll(@PathVariable UUID branchId) {
        ApiResponse apiResponse = salaryService.getAll(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_SALARY")
    @GetMapping("/{salaryId}")
    public HttpEntity<?> getOne(@PathVariable UUID salaryId) {
        ApiResponse apiResponse = salaryService.getOne(salaryId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_SALARY")
    @DeleteMapping("/{salaryId}")
    public HttpEntity<?> deleteOne(@PathVariable UUID salaryId) {
        ApiResponse apiResponse = salaryService.deleteOne(salaryId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
