package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.enums.BalanceType;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.service.BalanceService;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/balance")
public class BalanceController {
    private final BalanceService balanceService;

    @CheckPermission("EDIT_BALANCE")
    @PutMapping("/{branchId}")
    public HttpEntity<?> edit(@PathVariable UUID branchId,
                              @Valid @RequestBody Double summa,
                              @RequestBody List<UUID> payMethodId) {
        ApiResponse apiResponse = balanceService.edit(branchId, summa, true, payMethodId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_BALANCE")
    @GetMapping("/{branchId}")
    public HttpEntity<?> getAll(@PathVariable UUID branchId) {
        ApiResponse apiResponse = balanceService.getAll(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
