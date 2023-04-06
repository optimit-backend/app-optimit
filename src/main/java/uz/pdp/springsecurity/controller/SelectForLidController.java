package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LidFieldDto;
import uz.pdp.springsecurity.payload.SelectForLidDto;
import uz.pdp.springsecurity.service.SelectForLidService;

import java.util.UUID;

@RestController
@RequestMapping("/api/select-for-lid")
@RequiredArgsConstructor
public class SelectForLidController {
    private final SelectForLidService service;

    @CheckPermission("VIEW_FORM_LID")
    @GetMapping("/getByBusinessId/{businessId}")
    public HttpEntity<?> getAll(@PathVariable UUID businessId) {
        ApiResponse apiResponse = service.getAll(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_FORM_LID")
    @GetMapping("/{id}")
    public HttpEntity<?> getById(@PathVariable UUID id) {
        ApiResponse apiResponse = service.getById(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("ADD_FORM_LID")
    @PostMapping
    public HttpEntity<?> create(@RequestBody SelectForLidDto dto) {
        ApiResponse apiResponse = service.create(dto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_FORM_LID")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody SelectForLidDto dto) {
        ApiResponse apiResponse = service.edit(id, dto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_FORM_LID")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = service.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

}
