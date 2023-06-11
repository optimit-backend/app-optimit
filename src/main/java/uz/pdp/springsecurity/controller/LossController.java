package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LossDTO;
import uz.pdp.springsecurity.service.LossService;
import uz.pdp.springsecurity.utils.AppConstant;

import java.util.UUID;

@RestController
@RequestMapping("/api/loss")
@RequiredArgsConstructor
public class LossController {
   private final LossService lossService;
    @CheckPermission("ADD_TRADE")
    @PostMapping
    public HttpEntity<?> create(@RequestBody LossDTO lossDTO) {
        ApiResponse apiResponse = lossService.create(lossDTO);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("ADD_TRADE")
    @GetMapping("/by-branch/{branchId}")
    public HttpEntity<?> get(@PathVariable UUID branchId,
                             @RequestParam(defaultValue = AppConstant.DEFAULT_PAGE) int page,
                             @RequestParam(defaultValue = AppConstant.DEFAULT_SIZE) int size) {
        ApiResponse apiResponse = lossService.get(branchId, page, size);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("ADD_TRADE")
    @GetMapping("/{lossId}")
    public HttpEntity<?> getOne(@PathVariable UUID lossId) {
        ApiResponse apiResponse = lossService.getOne(lossId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}