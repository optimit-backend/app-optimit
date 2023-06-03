package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.entity.ExchangeProductByConfirmation;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.CarDto;
import uz.pdp.springsecurity.payload.ExchangeProductByConfirmationDto;
import uz.pdp.springsecurity.service.ExchangeProductByConfirmationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/exchangeProductByConfirmation")
@RequiredArgsConstructor
public class ExchangeProductByConfirmationController {

    private final ExchangeProductByConfirmationService service;

    @CheckPermission("STOREKEEPER")
    @PostMapping
    public HttpEntity<?> add(@RequestBody ExchangeProductByConfirmationDto byConfirmationDto) {
        ApiResponse apiResponse = service.add(byConfirmationDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("STOREKEEPER")
    @GetMapping("/get-by-id/{id}")
    public HttpEntity<?> get(@PathVariable UUID id) {
        ApiResponse apiResponse = service.get(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("STOREKEEPER")
    @GetMapping("/get-by-business-id/{businessId}")
    public HttpEntity<?> getByBusinessId(@PathVariable UUID businessId) {
        ApiResponse apiResponse = service.getByBusinessId(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("STOREKEEPER")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody ExchangeProductByConfirmationDto byConfirmationDto) {
        ApiResponse apiResponse = service.edit(id, byConfirmationDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


}
