package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.CustomerDto;
import uz.pdp.springsecurity.payload.RepaymentDto;
import uz.pdp.springsecurity.service.CustomerExcelService;
import uz.pdp.springsecurity.service.CustomerService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    private final CustomerExcelService customerExcelService;

    @CheckPermission("ADD_CUSTOMER")
    @PostMapping
    public HttpEntity<?> addCustomer(@Valid @RequestBody CustomerDto customerDto) {
        ApiResponse apiResponse = customerService.add(customerDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_CUSTOMER")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody CustomerDto customerDto) {
        ApiResponse apiResponse = customerService.edit(id, customerDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/{id}")
    public HttpEntity<?> get(@PathVariable UUID id) {
        ApiResponse apiResponse = customerService.get(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_CUSTOMER")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = customerService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/get-by-businessId/{businessId}")
    public HttpEntity<?> getAllByBusinessId(@PathVariable UUID businessId) {
        ApiResponse apiResponse = customerService.getAllByBusinessId(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/get-by-branchId/{branchId}")
    public HttpEntity<?> getAllByBranchId(@PathVariable UUID branchId) {
        ApiResponse apiResponse = customerService.getAllByBranchId(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/get-by-group/{groupId}")
    public HttpEntity<?> getAllByGroupId(@PathVariable UUID groupId) {
        ApiResponse apiResponse = customerService.getAllByGroupId(groupId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("ADD_CUSTOMER")
    @PostMapping("/repayment/{id}")
    public HttpEntity<?> addRepayment(@PathVariable UUID id, @RequestBody RepaymentDto repaymentDto) {
        ApiResponse response = customerService.repayment(id, repaymentDto);
        return ResponseEntity.status(response.isSuccess() ? 201 : 409).body(response);
    }

    @PostMapping("/import/{branchId}")
    public ResponseEntity<?> importCustomersFromExcel(@PathVariable UUID branchId, @RequestParam("file") MultipartFile file) throws IOException {
        ApiResponse apiResponse = customerExcelService.importCustomersFromExcel(file, branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 201 : 409).body(apiResponse);
    }

    @GetMapping("/export/{branchId}")
    public void exportCustomersToExcel(@PathVariable UUID branchId, HttpServletResponse response) throws IOException {
        byte[] excelData = customerExcelService.exportCustomersToExcel(branchId);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=customers.xlsx");
        response.getOutputStream().write(excelData);
    }

    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/get-by-lid-customer/{branchId}")
    public HttpEntity<?> getAllByLidCustomer(@PathVariable UUID branchId) {
        ApiResponse apiResponse = customerService.getAllByLidCustomer(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/get-customer-info/{customerId}")
    public HttpEntity<?> getCustomerInfo(@PathVariable UUID customerId) {
        ApiResponse apiResponse = customerService.getCustomerInfo(customerId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/get-customer-trade-info/{customerId}")
    public HttpEntity<?> getCustomerTradeInfo(@PathVariable UUID customerId) {
        ApiResponse apiResponse = customerService.getCustomerTradeInfo(customerId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/get-customer-prevented-info/{customerId}")
    public HttpEntity<?> getCustomerPreventedInfo(@PathVariable UUID customerId) {
        ApiResponse apiResponse = customerService.getCustomerPreventedInfo(customerId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_CUSTOMER")
    @GetMapping("/search/{branchId}")
    public HttpEntity<?> search(@PathVariable UUID branchId,
                                @RequestParam String name) {
        ApiResponse apiResponse = customerService.search(branchId,name);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

}
