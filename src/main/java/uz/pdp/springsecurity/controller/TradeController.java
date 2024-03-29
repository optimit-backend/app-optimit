package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TradeDTO;
import uz.pdp.springsecurity.service.TradeService;
import uz.pdp.springsecurity.utils.AppConstant;

import java.util.UUID;

@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class TradeController {
   private final TradeService tradeService;

    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-sorted-traders/{branchId}")
    public HttpEntity<?> getTraderByProduct(@PathVariable UUID branchId) {
        ApiResponse apiResponse = tradeService.getTradeByTrader(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("ADD_TRADE")
    @PostMapping
    public HttpEntity<?> create(@RequestBody TradeDTO tradeDTO) {
        ApiResponse apiResponse = tradeService.create(tradeDTO);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("EDIT_TRADE")
    @PutMapping("/{tradeId}")
    public HttpEntity<?> edit(@PathVariable UUID tradeId, @RequestBody TradeDTO tradeDTO) {
        ApiResponse apiResponse = tradeService.edit(tradeId, tradeDTO);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_TRADE")
    @GetMapping("/{id}")
    public HttpEntity<?> getOne(@PathVariable UUID id) {
        ApiResponse apiResponse = tradeService.getOne(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("DELETE_TRADE")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = tradeService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @PreAuthorize(value = "hasAnyAuthority('VIEW_TRADE', 'VIEW_TRADE_ADMIN')")
    @GetMapping("/get-by-filter/{id}")
    public HttpEntity<?> getAllByFilter(@PathVariable UUID id,
                                        @RequestParam(required = false) String invoice,
                                        @RequestParam(required = false) Boolean backing,
                                        @RequestParam(defaultValue = AppConstant.DEFAULT_PAGE) int page,
                                        @RequestParam(defaultValue = AppConstant.DEFAULT_SIZE) int size) {
        ApiResponse apiResponse = tradeService.getAllByFilter(id, invoice, backing, page, size);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-branchId/{branchId}")
    public HttpEntity<?> getAllByBranchId(@PathVariable UUID branchId) {
        ApiResponse apiResponse = tradeService.getAllByBranchId(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_TRADE_ADMIN")
    @GetMapping("/get-by-business/{businessId}")
    public HttpEntity<?> getAllByBusinessId(@PathVariable UUID businessId) {
        ApiResponse apiResponse = tradeService.getAllByBusinessId(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_REPORT")
    @GetMapping("/backing/{branchId}")
    public HttpEntity<?> getBacking(@PathVariable UUID branchId) {
        ApiResponse apiResponse = tradeService.getBacking(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_REPORT")
    @GetMapping("/backing-by-product/{branchId}")
    public HttpEntity<?> getBackingByProduct(@PathVariable UUID branchId,
                                             @RequestParam UUID productId,
                                             @RequestParam(defaultValue = AppConstant.DEFAULT_PAGE) int page,
                                             @RequestParam(defaultValue = AppConstant.DEFAULT_SIZE) int size) {
        ApiResponse apiResponse = tradeService.getBackingByProduct(branchId, productId, page, size);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}