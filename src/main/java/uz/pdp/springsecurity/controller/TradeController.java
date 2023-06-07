package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TradeDTO;
import uz.pdp.springsecurity.service.TradeService;

import java.sql.Timestamp;
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
    @PutMapping("/{trade_id}")
    public HttpEntity<?> edit(@PathVariable UUID trade_id, @RequestBody TradeDTO tradeDTO) {
        ApiResponse apiResponse = tradeService.edit(trade_id, tradeDTO);
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

    @CheckPermission("VIEW_MY_TRADE")
    @GetMapping("/get-by-traderId/{trader_id}")
    public HttpEntity<?> getAllByTrader(@PathVariable UUID trader_id) {
        ApiResponse apiResponse = tradeService.getAllByTraderId(trader_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-customerId/{customer_id}")
    public HttpEntity<?> getByCustomer(@PathVariable UUID customer_id) {
        ApiResponse apiResponse = tradeService.getByCustomerId(customer_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-PayDate/{payDate}")
    public HttpEntity<?> getByPayDate(@PathVariable Timestamp payDate){
        ApiResponse apiResponse = tradeService.getByPayDate(payDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-PayStatusId/{paymentStatus_id}")
    public HttpEntity<?> getByPayStatus(@PathVariable UUID paymentStatus_id) {
        ApiResponse apiResponse = tradeService.getByPayStatus(paymentStatus_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-PayMethodId/{payMethod_id}")
    public HttpEntity<?> getByPayMethod(@PathVariable UUID payMethod_id) {
        ApiResponse apiResponse = tradeService.getByPayMethod(payMethod_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-AddressId/{address_id}")
    public HttpEntity<?> getByAddress(@PathVariable UUID address_id) {
        ApiResponse apiResponse = tradeService.getByAddress(address_id);
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

}