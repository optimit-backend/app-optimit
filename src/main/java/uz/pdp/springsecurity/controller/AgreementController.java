package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ContentDto;
import uz.pdp.springsecurity.service.ContentService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/agreement")
public class AgreementController {
    @Autowired
    ContentService agreementService;

    @CheckPermission("CREATE_CONTENT")
    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody ContentDto agreementDto) {
        ApiResponse apiResponse = agreementService.add(agreementDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_CONTENT")
    @PutMapping("/{agreementId}")
    public HttpEntity<?> edit(@PathVariable UUID agreementId, @Valid @RequestBody ContentDto agreementDto) {
        ApiResponse apiResponse = agreementService.edit(agreementId, agreementDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_CONTENT")
    @GetMapping("/by-branch/{branchId}")
    public HttpEntity<?> getAll(@PathVariable UUID branchId) {
        ApiResponse apiResponse = agreementService.getAll(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_CONTENT")
    @GetMapping("/{agreementId}")
    public HttpEntity<?> getOne(@PathVariable UUID agreementId) {
        ApiResponse apiResponse = agreementService.getOne(agreementId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_CONTENT")
    @DeleteMapping("/{agreementId}")
    public HttpEntity<?> deleteOne(@PathVariable UUID agreementId) {
        ApiResponse apiResponse = agreementService.deleteOne(agreementId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
