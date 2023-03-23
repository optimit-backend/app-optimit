package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.service.ReportsService;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired
    ReportsService reportsService;
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/all-by-branch/{branchId}")
    public HttpEntity<?> getAllBranchAmount(@PathVariable UUID branchId,
                                              @RequestParam(required = false) UUID brandId,
                                              @RequestParam(required = false) UUID categoryId,
                                              @RequestParam(required = false) String production) {
        ApiResponse apiResponse = reportsService.allProductAmount(branchId,brandId,categoryId,production);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/trade-by-branch/{branchId}")
    public HttpEntity<?> getAllTradeProducts(@PathVariable UUID branchId,
                                              @RequestParam(required = false) UUID payMethodId,
                                              @RequestParam(required = false) UUID customerId,
                                              @RequestParam(required = false) Date startDate,
                                              @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.tradeProductByBranch(branchId,payMethodId,customerId,startDate,endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_REPORT")
    @GetMapping("/amounts-by-brand/{branchId}")
    public HttpEntity<?> getAllBrandAmount(@PathVariable UUID branchId,
                                           @RequestParam UUID brandId) {
        ApiResponse apiResponse = reportsService.allProductByBrand(branchId, brandId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/amounts-by-category/{branchId}/{categoryId}")
    public HttpEntity<?> getAllCategoryAmount(@PathVariable UUID branchId,
                                              @RequestParam UUID categoryId) {
        ApiResponse apiResponse = reportsService.allProductByCategory(branchId, categoryId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/amounts-branch/{branchId}")
    public HttpEntity<?> getAllDateByBrand(@PathVariable UUID branchId) {
        ApiResponse apiResponse = reportsService.allProductAmountByBranch(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/most-sale/{branchId}")
    public HttpEntity<?> mostSaleProducts(@PathVariable UUID branchId,
                                          @RequestParam(required = false) UUID categoryId,
                                          @RequestParam(required = false) UUID brandId,
                                          @RequestParam(required = false) Date startDate,
                                          @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.mostSaleProducts(branchId,categoryId,brandId,startDate,endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/most-un-sale/{branchId}")
    public HttpEntity<?> mostUnSaleProducts(@PathVariable UUID branchId) {
        ApiResponse apiResponse = reportsService.mostUnSaleProducts(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/purchase/{branchId}")
    public HttpEntity<?> purchaseReports(@PathVariable UUID branchId,
                                         @RequestParam(required = false) UUID supplierId,
                                         @RequestParam(required = false) Date startDate,
                                         @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.purchaseReports(branchId,supplierId,startDate,endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
     @GetMapping("/production/by-date/{branchId}")
    public HttpEntity<?> productionReports(@PathVariable UUID branchId) {
        ApiResponse apiResponse = reportsService.productionReports(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/delivery/{branchId}")
    public HttpEntity<?> deliveryPriceGet(@PathVariable UUID branchId) {
        ApiResponse apiResponse = reportsService.deliveryPriceGet(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/outlay/{branchId}")
    public HttpEntity<?> outlayReports(@PathVariable UUID branchId,
                                       @RequestParam(required = false) UUID categoryId,
                                       @RequestParam(required = false) Date startDate,
                                       @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.outlayReports(branchId,categoryId,startDate,endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/customer/{branchId}")
    public HttpEntity<?> customerReports(@PathVariable UUID branchId,
                                         @RequestParam(required = false) UUID customerId,
                                         @RequestParam(required = false) Date startDate,
                                         @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.customerReports(branchId, customerId, startDate, endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/benefit-by-branch/{branchId}")
    public HttpEntity<?> benefitByBranchReports(@PathVariable UUID branchId,
                                                @RequestParam(required = false) String date,
                                                @RequestParam(required = false) Date startDate,
                                                @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.dateBenefitAndLostByProductReports(branchId, date, startDate, endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/benefit-by-category/{branchId}")
    public HttpEntity<?> benefitByCategoryReports(@PathVariable UUID branchId,
                                                  @RequestParam(required = false) String date,
                                                  @RequestParam(required = false) Date startDate,
                                                  @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.benefitAndLostByCategoryReports(branchId, date, startDate, endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/benefit-by-brand/{branchId}")
    public HttpEntity<?> benefitByBrandReports(@PathVariable UUID branchId,
                                               @RequestParam(required = false) String date,
                                               @RequestParam(required = false) Date startDate,
                                               @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.benefitAndLostByBrandReports(branchId, date, startDate, endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/benefit-by-customer/{branchId}")
    public HttpEntity<?> benefitByCustomerReports(@PathVariable UUID branchId,
                                                  @RequestParam(required = false) String date,
                                                  @RequestParam(required = false) Date startDate,
                                                  @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.benefitAndLostByCustomerReports(branchId, date, startDate, endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
    @CheckPermission("VIEW_REPORT")
    @GetMapping("/products-report/{branchId}")
    public HttpEntity<?> productsReport(@PathVariable UUID branchId,
                                        @RequestParam(required = false) UUID customerId,
                                        @RequestParam(required = false) String date,
                                        @RequestParam(required = false) Date startDate,
                                        @RequestParam(required = false) Date endDate) {
        ApiResponse apiResponse = reportsService.productsReport(customerId, branchId, date, startDate, endDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


}
