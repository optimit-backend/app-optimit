package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.BonusDto;
import uz.pdp.springsecurity.service.BalanceService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BalanceController {
    private final BalanceService balanceService;

    @CheckPermission("ADD_BALANCE")
    @PostMapping("/{branchId}")
    public HttpEntity<?> add(@PathVariable UUID branchId) {
        ApiResponse apiResponse = balanceService.add(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_BALANCE")
    @PutMapping("/{branchId}")
    public HttpEntity<?> edit(@PathVariable UUID branchId, @Valid @RequestBody Double summa) {
        ApiResponse apiResponse = balanceService.edit(branchId, summa);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_BALANCE")
    @GetMapping("/{branchId}")
    public HttpEntity<?> getAll(@PathVariable UUID branchId) {
        ApiResponse apiResponse = balanceService.getAll(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
