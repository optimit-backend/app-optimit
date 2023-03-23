package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.annotations.CurrentUser;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProductBarcodeDto;
import uz.pdp.springsecurity.payload.ProductDto;
import uz.pdp.springsecurity.repository.ProductRepository;
import uz.pdp.springsecurity.service.ProductService;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;

    /**
     * YANGI PRODUCT QO'SHISH
     *
     * @param productDto
     * @return ApiResponse(success - > true message - > ADDED)
     */
    @CheckPermission("ADD_PRODUCT")
    @PostMapping()
    public HttpEntity<?> add(@Valid @RequestBody ProductDto productDto) throws ParseException {
        ApiResponse apiResponse = productService.addProduct(productDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }



    /**
     * PRODUCTNI EDIT QILISH
     *
     * @param id
     * @param productDto
     * @return ApiResponse(success - > false message - > EDITED)
     */
    @CheckPermission("EDIT_PRODUCT")
    @PutMapping("{id}")
    public HttpEntity<?> edit(@PathVariable UUID id, @RequestBody ProductDto productDto) {
        ApiResponse apiResponse = productService.editProduct(id, productDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


    /**
     * USERDA TEGISHLI BARCHA PRODUCTLARNI OLIB CHIQISH
     *
     * @param
     * @return ApiResponse(success - > true object - > value)
     */

    @CheckPermission("VIEW_PRODUCT")
    @GetMapping
    public HttpEntity<?> get(@CurrentUser User user) {
        ApiResponse apiResponse = productService.getAll(user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI BITTA PRODUCTNI YANI MAXSULOTNI OLIB CHIQISH
     *
     * @param id
     * @return ApiResponse(success - > true object - > value)
     */
    @CheckPermission("VIEW_PRODUCT")
    @GetMapping("/{id}")
    public HttpEntity<?> getOne(@PathVariable UUID id, @CurrentUser User user) {
        ApiResponse apiResponse = productService.getProduct(id, user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI DELETE QILSISH
     *
     * @param id
     * @return ApiResponse(success - > true message - > DELETED)
     */
    @CheckPermission("DELETE_PRODUCT")
    @DeleteMapping("/{id}")
    public HttpEntity<?> deleteOne(@PathVariable UUID id, @CurrentUser User user) {
        ApiResponse apiResponse = productService.deleteProduct(id, user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * BIR NECHATA PRODUCTNI KELGAN IDLAR BO'YICHA O'CHIRISH
     *
     * @param ids
     * @return ApiResponse(success - > true message - > DELETED)
     */

    @CheckPermission("DELETE_PRODUCT")
    @DeleteMapping("/delete-few")
    public HttpEntity<?> deleteFew(@RequestBody List<UUID> ids, @CurrentUser User user) {
        ApiResponse apiResponse = productService.deleteProducts(ids);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * PRODUCTNI BARCODENI OLIB CHIQISH
     *
     * @param barcode
     * @return ApiResponse(success - > true object - > value)
     */

    @CheckPermission("VIEW_PRODUCT")
    @GetMapping("/get-by-barcode/{barcode}")
    public HttpEntity<?> getByBarcode(@PathVariable String barcode, @CurrentUser User user) {
        ApiResponse apiResponse = productService.getByBarcode(barcode, user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * CATEGORY ID ORQALI MAHSULOTLARNI OLIB CHIQISH
     *
     * @param category_id
     * @return ApiResponse(success - > true object - > value)
     */

    @CheckPermission("VIEW_PRODUCT")
    @GetMapping("/get-by-category/{category_id}")
    public HttpEntity<?> getByCategory(@PathVariable UUID category_id, @CurrentUser User user) {
        ApiResponse apiResponse = productService.getByCategory(category_id, user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


    /**
     * BRANDIDLAR ORQALI MAHSULOTLARNI OLIB CHIQISH
     *
     * @param brand_id
     * @return ApiResponse(success - > true object - > value)
     */
    @CheckPermission("VIEW_PRODUCT")
    @GetMapping("/get-by-brand/{brand_id}")
    public HttpEntity<?> getByBrand(@PathVariable UUID brand_id)  {
        ApiResponse apiResponse = productService.getByBrand(brand_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }



    /**
     * BRANCHGA TEGISHLI BARCHA MAHSULOTLARNI OLIB CHIQISH
     *
     * @param branch_id
     * @return ApiResponse(success - > true object - > value)
     */
    @CheckPermission("VIEW_PRODUCT")
    @PostMapping("/get-by-branch-and-barcode/{branch_id}")
    public HttpEntity<?> getByBranch(@PathVariable UUID branch_id, @CurrentUser User user, @RequestBody ProductBarcodeDto productBarcodeDto) {
        ApiResponse apiResponse = productService.getByBranchAndBarcode(branch_id, user, productBarcodeDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_PRODUCT")
    @GetMapping("/get-by-branch/{branch_id}")
    public HttpEntity<?> getByBranchAndBarcode(@PathVariable UUID branch_id) {
        ApiResponse apiResponse = productService.getByBranch(branch_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_PRODUCT")
    @GetMapping("/get-by-branch-for-purchase-trade/{branch_id}")
    public HttpEntity<?> getByBranchForSearch(@PathVariable UUID branch_id) {
        ApiResponse apiResponse = productService.getByBranchForSearch(branch_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_PRODUCT")
    @GetMapping("/get-by-business/{business_id}")
    public HttpEntity<?> getByBusiness(@PathVariable UUID business_id,
                                       @RequestParam(required = false) UUID branch_id,
                                       @RequestParam(required = false) UUID brand_id) {
        ApiResponse apiResponse = productService.getByBusiness(business_id,branch_id,brand_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_PRODUCT")
    @GetMapping("/get-all-by-branch/{branchId}")
    public HttpEntity<?> getByBranch(@PathVariable UUID branchId) {
        ApiResponse apiResponse = productService.getByBranchProduct(branchId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }
}
