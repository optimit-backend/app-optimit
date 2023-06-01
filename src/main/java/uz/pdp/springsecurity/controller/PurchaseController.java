package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.PurchaseDto;
import uz.pdp.springsecurity.service.PurchaseService;

import java.util.UUID;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;

    @CheckPermission("ADD_PURCHASE")
    @PostMapping
    public HttpEntity<?> add(@RequestBody PurchaseDto purchaseDto) {
        ApiResponse apiResponse = purchaseService.add(purchaseDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_PURCHASE")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody PurchaseDto purchaseDto) {
        ApiResponse apiResponse = purchaseService.edit(id, purchaseDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * BUSINESS ID'SI ORQALI BARCHA XARIDLARNI OLIB CHIQISH
     *
     * @param businessId
     * @return ApiResponse(success - > true message - > FOUND)
     */
    @CheckPermission("VIEW_PURCHASE_ADMIN")
    @GetMapping("/get-by-business/{businessId}")
    public HttpEntity<?> getAllByBusiness(@PathVariable UUID businessId) {
        ApiResponse apiResponse = purchaseService.getAllByBusiness(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI XARIDNI OLIB CHIQISH
     *
     * @param id
     * @return ApiResponse(success - > true message - > FOUND)
     */
    @CheckPermission("VIEW_PURCHASE")
    @GetMapping("/{id}")
    public HttpEntity<?> getOne(@PathVariable UUID id) {
        ApiResponse apiResponse = purchaseService.getOne(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI XARIDNI OLIB DELETE QILISH
     *
     * @param id
     * @return ApiResponse(success - > true message - > DELETED)
     */
    @CheckPermission("DELETE_PURCHASE")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = purchaseService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * YETKAZIB BERUVCHI (SUPPLIER) ID'SI ORQALI BARCHA XARIDLARNI OLIB CHIQISH
     *
     * @param dealer_id
     * @return ApiResponse(success - > true message - > FOUND)
     */
    @CheckPermission("VIEW_PURCHASE")
    @GetMapping("get-purchase-by-dealerId/{dealer_id}")
    public HttpEntity<?> getByDealerId(@PathVariable UUID dealer_id) {
        ApiResponse apiResponse = purchaseService.getByDealerId(dealer_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * XARID STATUSI ORQALI BARCHA XARIDLARNI OLIB CHIQISH
     *
     * @param purchaseStatus_id
     * @return ApiResponse(success - > true message - > FOUND)
     */
    @CheckPermission("VIEW_PURCHASE")
    @GetMapping("get-purchase-by-purchaseStatus/{purchaseStatus_id}")
    public HttpEntity<?> getByPurchaseStatusId(@PathVariable UUID purchaseStatus_id) {
        ApiResponse apiResponse = purchaseService.getByPurchaseStatusId(purchaseStatus_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * TOLOV STATUSI ORQALI BARCHA XARIDLARNI OLIB CHIQISH
     *
     * @param paymentStatus_id
     * @return ApiResponse(success - > true message - > FOUND)
     */
    @CheckPermission("VIEW_PURCHASE")
    @GetMapping("get-purchase-by-paymentStatus/{paymentStatus_id}")
    public HttpEntity<?> getByPaymentStatusId(@PathVariable UUID paymentStatus_id) {
        ApiResponse apiResponse = purchaseService.getByPaymentStatusId(paymentStatus_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * FILIAL ID'SI ORQALI BARCHA XARIDLARNI OLIB CHIQISH
     *
     * @param branch_id
     * @return ApiResponse(success - > true message - > FOUND)
     */

    @CheckPermission("VIEW_PURCHASE")
    @GetMapping("get-purchase-by-branch/{branch_id}")
    public HttpEntity<?> getByBranchId(@PathVariable UUID branch_id) {
        ApiResponse apiResponse = purchaseService.getByBranchId(branch_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
