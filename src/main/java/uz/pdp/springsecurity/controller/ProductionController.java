package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProductionDto;
import uz.pdp.springsecurity.service.ProductionService;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/production")
public class ProductionController {
    @Autowired
    ProductionService productionService;
    @CheckPermission("CREATE_PRODUCTION")
    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody ProductionDto productionDto) {
        ApiResponse apiResponse = productionService.add(productionDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_PRODUCTION")
    @GetMapping("/by-branch/{branchId}")
    public HttpEntity<?> getAll(@PathVariable UUID branchId) {
        ApiResponse apiResponse = productionService.getAll(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("GET_PRODUCTION")
    @GetMapping("/{productionId}")
    public HttpEntity<?> getOne(@PathVariable UUID productionId) {
        ApiResponse apiResponse = productionService.getOne(productionId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
