package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TradeDTO;
import uz.pdp.springsecurity.repository.TradeRepository;
import uz.pdp.springsecurity.service.TradeService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

@RestController
@RequestMapping("/api/trade")
public class TradeController {
    @Autowired
    TradeService tradeService;

    @Autowired
    TradeRepository tradeRepository;

    /**
     * YANGI SAVDO QOSHISH
     *
     * @param tradeDTO
     * @return ApiResponse(success - > true, message - > ADDED)
     */
    @CheckPermission("ADD_TRADE")
    @PostMapping
    public HttpEntity<?> create(@RequestBody TradeDTO tradeDTO) {
        ApiResponse apiResponse = tradeService.create(tradeDTO);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI SAVDONI TAHRIRLASH
     *
     * @param tradeDTO
     * @return ApiResponse(success - > true, message - > EDITED)
     */
    @CheckPermission("EDIT_TRADE")
    @PutMapping("/{trade_id}")
    public HttpEntity<?> edit(@PathVariable UUID trade_id, @RequestBody TradeDTO tradeDTO) {
        ApiResponse apiResponse = tradeService.edit(trade_id, tradeDTO);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI BITTA SAVDONI OLIB CHIQISH
     * @id id
     * @return ApiResponse(success - > true, object - > value)
     */
    @CheckPermission("VIEW_TRADE")
    @GetMapping("/{id}")
    public HttpEntity<?> getOne(@PathVariable UUID id) {
        ApiResponse apiResponse = tradeService.getOne(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI BITTA SAVDONI DELETE QILISH
     *
     * @param id
     * @return ApiResponse(success - > true, message - > DELETED)
     */
    @CheckPermission("DELETE_TRADE")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable UUID id) {
        ApiResponse apiResponse = tradeService.deleteTrade(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * SAVDOGAR ID'SI ORQALI BITTA SAVDONI DELETE QILISH
     *
     * @param trader_id
     * @return ApiResponse(success - > true, message - > DELETED)
     */
    @CheckPermission("DELETE_MY_TRADE")
    @DeleteMapping("/delete-by-traderId/trader_id")
    public HttpEntity<?> deleteByTraderId(@PathVariable UUID trader_id) {
        ApiResponse apiResponse = tradeService.deleteByTraderId(trader_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * SAVDOGAR ID'SI ORQALI BARCHA SAVDOLARNI DELETE QILISH
     *
     * @param trader_id
     * @return ApiResponse(success - > true, message - > DELETED)
     */
    @CheckPermission("DELETE_MY_TRADE")
    @DeleteMapping("/deleteAll-by-tradeId/trader_id")
    public HttpEntity<?> deleteAllByTraderId(@PathVariable UUID trader_id) {
        ApiResponse apiResponse = tradeService.deleteAllByTraderId(trader_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * SAVDOGAR ID'SI ORQALI BARCHA SAVDOLARNI OLIB CHIQISH
     *
     * @param trader_id
     * @return ApiResponse(success - > true, message - > FOUND)
     */
    @CheckPermission("VIEW_MY_TRADE")
    @GetMapping("/get-by-traderId/{trader_id}")
    public HttpEntity<?> getAllByTrader(@PathVariable UUID trader_id) {
        ApiResponse apiResponse = tradeService.getAllByTraderId(trader_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * MIJOZ ID'SI ORQALI BARCHA SAVDOLARNI OLIB CHIQISH
     * @param customer_id
     * @return ApiResponse(success - > true, message - > FOUND)
     */
    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-customerId/{customer_id}")
    public HttpEntity<?> getByCustomer(@PathVariable UUID customer_id) {
        ApiResponse apiResponse = tradeService.getByCustomerId(customer_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * TOLOV SANASI ORQALI BARCHA SAVDOLARNI OLIB CHIQISH
     *
     * @param payDate
     * @return ApiResponse(success - > true, message - > FOUND)
     */
    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-PayDate/{payDate}")
    public HttpEntity<?> getByPayDate(@PathVariable Timestamp payDate) throws ParseException {
        ApiResponse apiResponse = tradeService.getByPayDate(payDate);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * TOLOV STATUS ID'SI ORQALI BARCHA SAVDOLARNI OLIB CHIQISH
     *
     * @param paymentStatus_id
     * @return ApiResponse(success - > true, message - > FOUND)
     */
    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-PayStatusId/{paymentStatus_id}")
    public HttpEntity<?> getByPayStatus(@PathVariable UUID paymentStatus_id) {
        ApiResponse apiResponse = tradeService.getByPayStatus(paymentStatus_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * TOLOV USULI ID'SI ORQALI BARCHA SAVDOLARNI OLIB CHIQISH
     *
     * @param payMethod_id
     * @return ApiResponse(success - > true, message - > FOUND)
     */
    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-PayMethodId/{payMethod_id}")
    public HttpEntity<?> getByPayMethod(@PathVariable UUID payMethod_id) {
        ApiResponse apiResponse = tradeService.getByPayMethod(payMethod_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * YETKAZIB BERISH MANZILI ORQALI BARCHA SAVDOLARNI OLIB CHIQISH
     *
     * @param address_id
     * @return ApiResponse(success - > true, message - > FOUND)
     */
    @CheckPermission("VIEW_TRADE")
    @GetMapping("/get-by-AddressId/{address_id}")
    public HttpEntity<?> getByAddress(@PathVariable UUID address_id) {
        ApiResponse apiResponse = tradeService.getByAddress(address_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * SAVDO ID'SI ORQALI SAVDONING PDF CHEKINI DOWNLOAD QILISH
     *
     * @param id
     * @return ApiResponse(success - > true, message - > CREATED)
     */
    @CheckPermission("DOWNLOAD_MEDIA")
    @GetMapping("/get-pdf/{id}")
    public HttpEntity<?> getPdf(@PathVariable UUID id, HttpServletResponse response) throws IOException {
        ApiResponse apiResponse = tradeService.createPdf(id, response);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * BUSINESS ID'SI ORQALI BARCHA SAVDOLARNI OLIB CHIQISH
     *
     * @param businessId
     * @return ApiResponse(success - > true, message - > FOUND)
     */
    @CheckPermission("VIEW_TRADE_ADMIN")
    @GetMapping("/get-by-business/{businessId}")
    public HttpEntity<?> getAllByBusinessId(@PathVariable UUID businessId) {
        ApiResponse apiResponse = tradeService.getAllByBusinessId(businessId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

}