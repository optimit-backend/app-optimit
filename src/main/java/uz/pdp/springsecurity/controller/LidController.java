package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LidDto;
import uz.pdp.springsecurity.service.LidService;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/lid")
@RequiredArgsConstructor
public class LidController {
    private final LidService lidService;

    @GetMapping("/get-by-businessId/{businessId}")
    HttpEntity<?> getAll(@PathVariable UUID businessId) {
        ApiResponse apiResponse = lidService.getAll(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @GetMapping("/get-by-id/{id}")
    HttpEntity<?> getById(@PathVariable UUID id) {
        ApiResponse apiResponse = lidService.getById(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @GetMapping("/get-by-businessId-pageable/{id}")
    HttpEntity<?> getByBusinessIdPageable(@PathVariable UUID id) {
        ApiResponse apiResponse = lidService.getByBusinessIdPageable(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


    @PostMapping
    HttpEntity<?> create(@RequestBody LidDto lidDto) {
        ApiResponse apiResponse = lidService.create(lidDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @PutMapping("/edit-status/{id}")
    HttpEntity<?> editStatus(@PathVariable UUID id,
                             @RequestParam UUID statusId) {
        ApiResponse apiResponse = lidService.editStatus(id, statusId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @DeleteMapping("/{id}")
    HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = lidService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
