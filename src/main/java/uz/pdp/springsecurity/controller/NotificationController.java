package uz.pdp.springsecurity.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CurrentUser;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.service.NotificationService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public HttpEntity<?> getAll(@CurrentUser User user) {
        ApiResponse apiResponse = notificationService.getAll(user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @GetMapping("/{id}")
    public HttpEntity<?> getById(@PathVariable UUID id) {
        ApiResponse apiResponse = notificationService.getById(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @DeleteMapping
    public HttpEntity<?> deleteAll(@CurrentUser User user) {
        ApiResponse apiResponse = notificationService.delete(user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
