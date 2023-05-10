package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ShablonDto;
import uz.pdp.springsecurity.payload.SmsDto;
import uz.pdp.springsecurity.service.ShablonService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shablon")
public class ShablonController {

    private final ShablonService shablonService;

    @PostMapping
    public HttpEntity<?> addCustomer(@Valid @RequestBody ShablonDto shablonDto) {
        ApiResponse apiResponse = shablonService.add(shablonDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @GetMapping("/getAll")
    public HttpEntity<?> getAll() {
        ApiResponse apiResponse = shablonService.getAll();
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @GetMapping("/{id}")
    public HttpEntity<?> getById(@PathVariable UUID id) {
        ApiResponse apiResponse = shablonService.getById(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@Valid @RequestBody ShablonDto shablonDto,@PathVariable UUID id) {
        ApiResponse apiResponse = shablonService.edit(shablonDto,id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = shablonService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
