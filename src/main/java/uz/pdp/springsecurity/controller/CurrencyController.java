package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.CurrencyDto;
import uz.pdp.springsecurity.service.CurrencyService;

import java.util.UUID;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;
    @GetMapping("/{businessId}")
    public HttpEntity<?> getByBusiness(@PathVariable UUID businessId){
        ApiResponse response = currencyService.getByBusiness(businessId);
        return ResponseEntity.status(response.isSuccess()? 200:409).body(response);
    }

    @PutMapping("/{businessId}")
    public HttpEntity<?> edit(@PathVariable UUID businessId, @RequestBody CurrencyDto currencyDto){
        ApiResponse response = currencyService.edit(businessId, currencyDto);
        return ResponseEntity.status(response.isSuccess()? 200:409).body(response);
    }

    /*@CheckPermission("ADD_CURRENCY")
    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody CurrencyDto currencyDto) {
        ApiResponse apiResponse = currencyService.add(currencyDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }*/

    /*@CheckPermission("DELETE_CURRENCY")
    @DeleteMapping("/{id}")
    public HttpEntity<?> deleteCurrency(@PathVariable UUID id){
        ApiResponse response = currencyService.delete(id);
        return ResponseEntity.ok(response);
    }*/

    /*@CheckPermission("VIEW_CURRENCY")
    @GetMapping("/getAll/{businessId}")
    public HttpEntity<?> getAllCurrency(@PathVariable UUID businessId){
        ApiResponse response = currencyService.getAllCurrency(businessId);
        return ResponseEntity.status(response.isSuccess()? 200:409).body(response);
    }*/

    /*@CheckPermission("VIEW_CURRENCY")
    @GetMapping("/{id}")
    public HttpEntity<?> getOneCurrency(@PathVariable UUID id){
        ApiResponse response = currencyService.getOneCurrency(id);
        return ResponseEntity.status(response.isSuccess()? 200 : 409).body(response);
    }*/

    /*@CheckPermission("EDIT_CURRENCY")
    @PutMapping("/{id}")
    public HttpEntity<?> editOneCurrency(@PathVariable UUID id, @RequestBody CurrencyDto dto){
        ApiResponse response = currencyService.editCurrency(id, dto);
        return ResponseEntity.status(response.isSuccess()? 200:409).body(response);
    }*/

    /*@CheckPermission("EDIT_CURRENCY")
    @PutMapping("/editCourse/{id}")
    public HttpEntity<?> editCourse(@PathVariable UUID id, @RequestParam double course){
        ApiResponse response = currencyService.editCourse(id, course);
        return ResponseEntity.status(response.isSuccess()? 200 : 409).body(response);
    }*/

    /*@CheckPermission("EDIT_CURRENCY")
    @PutMapping("active-course")
    public HttpEntity<?> activeCourseEdit(@RequestBody EditCourse editCourse){
        ApiResponse response = currencyService.editActiveCourse(editCourse);
        return ResponseEntity.status(response.isSuccess()? 200:409).body(response);
    }*/
}
