package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.BusinessDto;
import uz.pdp.springsecurity.payload.BusinessEditDto;
import uz.pdp.springsecurity.payload.CheckDto;
import uz.pdp.springsecurity.service.BusinessService;

import java.util.UUID;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    BusinessService businessService;

    /**
     * YANGI BUSINESS QO'SHISH
     *
     * @param businessDto
     * @return ApiResponse(success - > true message - > ADDED)
     */
    @PostMapping("/create")
    public HttpEntity<?> add(@RequestBody BusinessDto businessDto) {
        ApiResponse apiResponse = businessService.add(businessDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * BUSINESSNI IDSI ORQALI EDIT QILISH
     *
     * @param id
     * @param businessEditDto
     * @return ApiResponse(success - > true message - > EDITED)
     */
    @CheckPermission("EDIT_BUSINESS")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody BusinessEditDto businessEditDto) {
        ApiResponse apiResponse = businessService.edit(id, businessEditDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * BITTA ID ORQALI BUSINESSNI OLIB CHIQISH
     *
     * @param id
     * @return ApiResponse(success - > true object - > value)
     */
    @CheckPermission("VIEW_BUSINESS")
    @GetMapping("/{id}")
    public HttpEntity<?> getOne(@PathVariable UUID id) {
        ApiResponse apiResponse = businessService.getOne(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * @return
     */


    @CheckPermission("VIEW_BUSINESS")
    @GetMapping("/partners")
    public HttpEntity<?> getAllPartners() {
        ApiResponse apiResponse = businessService.getAllPartners();
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI O'CHIRISH
     *
     * @param id
     * @return ApiResponse(success - > true object - > value)
     */
    @CheckPermission("DELETE_BUSINESS")
    @DeleteMapping("/{id}")
    public HttpEntity<?> deleteOne(@PathVariable UUID id) {
        ApiResponse apiResponse = businessService.deleteOne(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_BUSINESS")
    @GetMapping("/all")
    public HttpEntity<?> getAll() {
        ApiResponse apiResponse = businessService.getAll();
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_BUSINESS")
    @PutMapping("/de-active/{businessId}")
    public HttpEntity<?> deActive(@PathVariable UUID businessId) {
        ApiResponse apiResponse = businessService.deActive(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_BUSINESS")
    @GetMapping("/info")
    public HttpEntity<?> getInfo(@RequestParam String time) {
        ApiResponse apiResponse = businessService.getInfo(time);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @PostMapping("/checkBusinessName")
    public HttpEntity<?> checkBusinessName(@RequestBody CheckDto checkDto) {
        ApiResponse apiResponse = businessService.checkBusinessName(checkDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @PostMapping("/checkUsername")
    public HttpEntity<?> checkUsername(@RequestBody CheckDto checkDto) {
        ApiResponse apiResponse = businessService.checkUsername(checkDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
