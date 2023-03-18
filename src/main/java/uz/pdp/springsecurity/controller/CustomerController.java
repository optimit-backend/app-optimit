package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.CustomerDto;
import uz.pdp.springsecurity.payload.RepaymentDto;
import uz.pdp.springsecurity.repository.CustomerRepository;
import uz.pdp.springsecurity.service.CustomerService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerService customerService;

    /**
     * YANGI CUSTOMER QO'SHISH MIJOZ YANI
     *
     * @param customerDto
     * @return ApiResponse(success - > true message - > ADDED)
     */
    @CheckPermission("ADD_CUSTOMER")
    @PostMapping
    public HttpEntity<?> addCustomer(@Valid @RequestBody CustomerDto customerDto) {
        ApiResponse apiResponse = customerService.add(customerDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * CUSTOMERNI EDIT QILSIH ID ORQALI
     *
     * @param id
     * @param customerDto
     * @return ApiResponse(success - > true message - > EDITED)
     */
    @CheckPermission("EDIT_CUSTOMER")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody CustomerDto customerDto) {
        ApiResponse apiResponse = customerService.edit(id, customerDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI BITTA MIJOZNI CUSTOMERNI OLIB CIQISH
     *
     * @param id
     * @return ApiResponse(success - > true object - > value)
     */
    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/{id}")
    public HttpEntity<?> get(@PathVariable UUID id) {
        ApiResponse apiResponse = customerService.get(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * CUSTOMERNI MIJOZNI DELETE QILSIH
     *
     * @param id
     * @return ApiResponse(success - > true message - > DELETED)
     */
    @CheckPermission("DELETE_CUSTOMER")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = customerService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * BUSINESSGA TEGISHLI BARCHA CUSTOMERLARNI OLIB CHIQISH
     *
     * @param businessId
     * @return ApiResponse(success - > true object - > value)
     */
    @CheckPermission("VIEW_CUSTOMER_ADMIN")
    @GetMapping("/get-by-businessId/{businessId}")
    public HttpEntity<?> getAllByBusinessId(@PathVariable UUID businessId) {
        ApiResponse apiResponse = customerService.getAllByBusinessId(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_CUSTOMER_ADMIN")
    @GetMapping("/get-by-branchId/{branchId}")
    public HttpEntity<?> getAllByBranchId(@PathVariable UUID branchId) {
        ApiResponse apiResponse = customerService.getAllByBranchId(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }



    /**
     * QARZNI TO'LASH
     *
     */
    @CheckPermission("ADD_CUSTOMER")
    @PostMapping("/repayment/{id}")
    public HttpEntity<?> addRepayment(@PathVariable UUID id, @RequestBody RepaymentDto repaymentDto){
        ApiResponse response = customerService.repayment(id, repaymentDto);
        return ResponseEntity.status(response.isSuccess() ? 201 : 409 ).body(response);
    }
}
