package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.entity.Currency;
import uz.pdp.springsecurity.enums.Type;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.*;


@Service
@RequiredArgsConstructor
public class ProductService {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MeasurementRepository measurementRepository;

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CurrentCourceRepository currentCourceRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    ProductTypePriceRepository productTypePriceRepository;

    @Autowired
    ProductTypeValueRepository productTypeValueRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    private final ProductTypeComboRepository comboRepository;
    private final RoleRepository roleRepository;


    public ApiResponse addProduct(@Valid ProductDto productDto) throws ParseException {

        for (UUID integer : productDto.getBranchId()) {
            Optional<Product> optionalProduct = productRepository.findAllByBarcodeAndBranchIdAndActiveTrue(productDto.getBarcode(), integer);
            if (optionalProduct.isPresent()) {
                return new ApiResponse("PRODUCT WITH SAME BARCODE ALREADY EXISTS", false);
            }
        }

        UUID businessId = productDto.getBusinessId();
        UUID categoryId = productDto.getCategoryId();
        UUID brandId = productDto.getBrandId();
        UUID measurementId = productDto.getMeasurementId();
        List<UUID> photoIds = productDto.getPhotoIds();
        List<UUID> branchId = productDto.getBranchId();

        Optional<Business> optionalBusiness = businessRepository.findById(businessId);
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        Optional<Brand> optionalBrand = brandRepository.findById(brandId);
        Optional<Measurement> optionalMeasurement = measurementRepository.findById(measurementId);
        List<Branch> allBranch = branchRepository.findAllById(branchId);
        List<Attachment> attachmentList = attachmentRepository.findAllById(photoIds);


        if (optionalBusiness.isEmpty())
            return new ApiResponse("not found business", false);

        if (optionalCategory.isEmpty())
            return new ApiResponse("not found category", false);

        if (optionalBrand.isEmpty())
            return new ApiResponse("not found brand", false);

        if (optionalMeasurement.isEmpty())
            return new ApiResponse("not found measurement", false);


        Product product = new Product();
        product.setName(productDto.getName());
        product.setBranch(allBranch);
        product.setBusiness(optionalBusiness.get());
        product.setCategory(optionalCategory.get());
        product.setBrand(optionalBrand.get());
        product.setMeasurement(optionalMeasurement.get());
        product.setPhoto(attachmentList);
        product.setTax(productDto.getTax());
        product.setBarcode(productDto.getBarcode());
        product.setExpireDate(productDto.getExpireDate());
        product.setMinQuantity(productDto.getQuantity());
        product.setDueDate(productDto.getDueDate());

        product.setActive(true);

        boolean isUpdate = false;

        if (productDto.getType().equals(Type.SINGLE.name())) {
            return addProductTypeSingleDto(productDto, product);
        } else if (productDto.getType().equals(Type.MANY.name())) {
            return addProductTypeManyDto(productDto, product, isUpdate);
        } else {
            return addProductTypeComboDto(productDto, product, isUpdate);
        }

    }

    private ApiResponse addProductTypeComboDto(ProductDto productDto, Product product, boolean isUpdate) {

        product.setType(Type.COMBO);
        Product saveProduct = productRepository.save(product);

        List<ProductTypeComboDto> productTypeComboDtoList = productDto.getProductTypeComboDtoList();

        List<ProductTypeCombo> productTypeComboList = new ArrayList<>();

        for (ProductTypeComboDto productTypeComboDto : productTypeComboDtoList) {
            Optional<Product> optionalProduct = productRepository.findById(productTypeComboDto.getContentProductId());
            if (optionalProduct.isEmpty()) {
                return new ApiResponse("not found content product", false);
            }
            if (isUpdate) {
                List<ProductTypeComboDto> typeComboDtoList = productDto.getProductTypeComboDtoList();
                for (ProductTypeComboDto typeComboDto : typeComboDtoList) {
                    Optional<ProductTypeCombo> comboOptional = comboRepository.findById(typeComboDto.getComboId());
                    if (comboOptional.isEmpty()) {
                        return new ApiResponse("not found combo product", false);
                    }
                    ProductTypeCombo productTypeCombo = comboOptional.get();
                    productTypeCombo.setMainProduct(saveProduct);
                    productTypeCombo.setContentProduct(optionalProduct.get());
                    productTypeCombo.setAmount(productTypeComboDto.getAmount());
                    productTypeCombo.setBuyPrice(productTypeComboDto.getBuyPrice());
                    productTypeCombo.setSalePrice(productTypeComboDto.getSalePrice());
//                    productTypeCombo.setMeasurement(saveProduct.getMeasurement());
                    productTypeComboList.add(productTypeCombo);
                }
            } else {
                ProductTypeCombo productTypeCombo = new ProductTypeCombo();
                productTypeCombo.setMainProduct(saveProduct);
//                productTypeCombo.setMeasurement(saveProduct.getMeasurement());
                productTypeCombo.setContentProduct(optionalProduct.get());
                productTypeCombo.setAmount(productTypeComboDto.getAmount());
                productTypeCombo.setBuyPrice(productTypeComboDto.getBuyPrice());
                productTypeCombo.setSalePrice(productTypeComboDto.getSalePrice());
                productTypeComboList.add(productTypeCombo);
            }
        }
        saveProduct.setBuyPrice(productDto.getBuyPrice());
        saveProduct.setTax(productDto.getTax());
        saveProduct.setSalePrice(productDto.getSalePrice());
        comboRepository.saveAll(productTypeComboList);
        productRepository.save(saveProduct);
        return new ApiResponse("successfully saved",true);
    }

    private ApiResponse addProductTypeSingleDto(ProductDto productDto, Product product) {
        product.setType(Type.SINGLE);
        product.setBarcode(productDto.getBarcode());
        product.setBuyPrice(productDto.getBuyPrice());
        product.setSalePrice(productDto.getSalePrice());


        productRepository.save(product);

        return new ApiResponse("successfully added", true);
    }


    private ApiResponse addProductTypeManyDto(ProductDto productDto, Product product, boolean isUpdate) {
        product.setType(Type.MANY);

        Product saveProduct = productRepository.save(product);
        List<ProductTypePrice> productTypePriceList = new ArrayList<>();

        for (ProductTypePricePostDto typePricePostDto : productDto.getProductTypePricePostDtoList()) {
            Optional<ProductTypeValue> optionalProductTypeValue = productTypeValueRepository.findById(typePricePostDto.getProductTypeValueId());
            if (optionalProductTypeValue.isPresent()) {
                if (isUpdate) {
                    Optional<ProductTypePrice> typePriceOptional = productTypePriceRepository.findById(typePricePostDto.getProductTypePriceId());
                    if (typePriceOptional.isEmpty()) {
                        return new ApiResponse("not found product type many id", false);
                    }
                    ProductTypePrice productTypePrice = typePriceOptional.get();
                    productTypePrice.setProduct(saveProduct);
                    productTypePrice.setProductTypeValue(optionalProductTypeValue.get());
                    productTypePrice.setBuyPrice(typePricePostDto.getBuyPrice());
                    productTypePrice.setSalePrice(typePricePostDto.getSalePrice());
                    productTypePrice.setProfitPercent(typePricePostDto.getProfitPercent());
                    productTypePrice.setBarcode(typePricePostDto.getBarcode());
                    productTypePriceList.add(productTypePrice);
                } else {
                    ProductTypePrice productTypePrice = new ProductTypePrice();
                    productTypePrice.setProduct(saveProduct);
                    productTypePrice.setProductTypeValue(optionalProductTypeValue.get());
                    productTypePrice.setBuyPrice(typePricePostDto.getBuyPrice());
                    productTypePrice.setSalePrice(typePricePostDto.getSalePrice());
                    productTypePrice.setProfitPercent(typePricePostDto.getProfitPercent());
                    productTypePrice.setBarcode(typePricePostDto.getBarcode());
                    productTypePriceList.add(productTypePrice);
                }
            }
        }

        if (productTypePriceList.size() > 0) {
            productTypePriceRepository.saveAll(productTypePriceList);
            return new ApiResponse("successfully saved", true);
        }

        return new ApiResponse("NOT FOUND PRODUCT TYPE VALUE", false);
    }


    public ApiResponse editProduct(UUID id, ProductDto productDto) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }

        UUID businessId = productDto.getBusinessId();
        UUID categoryId = productDto.getCategoryId();
        UUID brandId = productDto.getBrandId();
        UUID measurementId = productDto.getMeasurementId();
        List<UUID> photoIds = productDto.getPhotoIds();
        List<UUID> branchId = productDto.getBranchId();

        Optional<Business> optionalBusiness = businessRepository.findById(businessId);
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        Optional<Brand> optionalBrand = brandRepository.findById(brandId);
        Optional<Measurement> optionalMeasurement = measurementRepository.findById(measurementId);
        List<Branch> allBranch = branchRepository.findAllById(branchId);
        List<Attachment> attachmentList = attachmentRepository.findAllById(photoIds);


        if (optionalBusiness.isEmpty())
            return new ApiResponse("not found business", false);

        if (optionalCategory.isEmpty())
            return new ApiResponse("not found category", false);

        if (optionalBrand.isEmpty())
            return new ApiResponse("not found brand", false);

        if (optionalMeasurement.isEmpty())
            return new ApiResponse("not found measurement", false);


        Product product = optionalProduct.get();
        product.setName(productDto.getName());
        product.setBranch(allBranch);
        product.setBusiness(optionalBusiness.get());
        product.setCategory(optionalCategory.get());
        product.setBrand(optionalBrand.get());
        product.setMeasurement(optionalMeasurement.get());
        product.setPhoto(attachmentList);
        product.setBarcode(productDto.getBarcode());
        product.setActive(true);

        boolean isUpdate = true;

        if (productDto.getType().equals(Type.SINGLE.name())) {
            return addProductTypeSingleDto(productDto, product);
        } else if (productDto.getType().equals(Type.MANY.name())) {
            return addProductTypeManyDto(productDto, product, isUpdate);
        } else {
            return addProductTypeComboDto(productDto, product, isUpdate);
        }

    }


    public ApiResponse getAll(User user) {
        UUID businessId = user.getBusiness().getId();
        Set<Branch> branches = user.getBranches();
        List<Product> productList = new ArrayList<>();
        for (Branch branch : branches) {
            List<Product> all = productRepository.findAllByBranchIdAndActiveTrue(branch.getId());
            if (!all.isEmpty()) {
                for (Product product : all) {
                    Currency currency = currencyRepository.findByBusinessIdAndActiveTrue(businessId);
                    if (!currency.getName().equalsIgnoreCase("SO'M")) {
                        CurrentCource cource = currentCourceRepository.getByCurrencyId(currency.getId());
                        product.setSalePrice(product.getSalePrice() / cource.getCurrentCourse());
                        product.setBuyPrice(product.getBuyPrice() / cource.getCurrentCourse());
                    } else {
                        break;
                    }
                }
                productList.addAll(all);
            }
        }
        if (productList.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }
        return new ApiResponse("FOUND", true, productList);
    }

    public ApiResponse getProduct(UUID id, User user) {
        Set<Branch> branches = user.getBranches();
        for (Branch branch : branches) {
            Optional<Product> optionalProduct = productRepository.findByIdAndBranchIdAndActiveTrue(id, branch.getId());
            if (optionalProduct.isPresent()) {
                return getProductHelper(optionalProduct.get());
            }
        }
        return new ApiResponse("NOT FOUND", false);
    }

    public ApiResponse getProductHelper(Product product) {
        ProductGetDto productGetDto = new ProductGetDto(product);

        if (product.getType().name().equals(Type.SINGLE.name())) {
            return new ApiResponse("productGetDto", true, productGetDto);
        } else if (product.getType().name().equals(Type.MANY.name())) {
            List<ProductTypePriceGetDto> productTypePriceGetDtoList = new ArrayList<>();
            List<ProductTypePrice> allByProductId = productTypePriceRepository.findAllByProductId(product.getId());
            for (ProductTypePrice productTypePrice : allByProductId) {
                ProductTypePriceGetDto productTypePriceGetDto = new ProductTypePriceGetDto();

                productTypePriceGetDto.setProductTypeName(productTypePrice.getProductTypeValue().getProductType().getName());
                productTypePriceGetDto.setProductTypeValueName(productTypePrice.getProductTypeValue().getName());
                productTypePriceGetDto.setBarcode(productTypePrice.getBarcode());
                productTypePriceGetDto.setProfitPercent(productTypePrice.getProfitPercent());
                productTypePriceGetDto.setBuyPrice(productTypePrice.getBuyPrice());
                productTypePriceGetDto.setSalePrice(productTypePrice.getSalePrice());

                productTypePriceGetDtoList.add(productTypePriceGetDto);
            }
            productGetDto.setProductTypePriceGetDtoList(productTypePriceGetDtoList);
            return new ApiResponse("productGetDto", true, productGetDto);

        } else {

            List<ProductTypeComboGetDto> comboGetDtoList = new ArrayList<>();
            List<ProductTypeCombo> allComboProduct = comboRepository.findAllByMainProductId(product.getId());
            for (ProductTypeCombo combo : allComboProduct) {
                ProductTypeComboGetDto comboGetDto = new ProductTypeComboGetDto();
                comboGetDto.setContentProduct(combo.getContentProduct());
                comboGetDto.setAmount(combo.getAmount());
                comboGetDto.setBuyPrice(combo.getBuyPrice());
                comboGetDto.setSalePrice(combo.getSalePrice());

                comboGetDtoList.add(comboGetDto);
            }

            productGetDto.setComboGetDtoList(comboGetDtoList);
            return new ApiResponse("productGetDto", true, productGetDto);
        }
    }

    public ApiResponse deleteProduct(UUID id, User user) {
        Set<Branch> branches = user.getBranches();
        for (Branch branch : branches) {
            Optional<Product> optionalProduct = productRepository.findByIdAndBranchIdAndActiveTrue(id, branch.getId());
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                product.setActive(false);
                productRepository.save(product);
                return new ApiResponse("DELETED", true);
            }
        }
        return new ApiResponse("NOT FOUND", false);
    }


    public ApiResponse getByBarcode(String barcode, User user) {
        Set<Branch> branches = user.getBranches();
        List<Product> productAllByBarcode = new ArrayList<>();
        for (Branch branch : branches) {
            Optional<Product> optionalProduct = productRepository.findAllByBarcodeAndBranchIdAndActiveTrue(barcode, branch.getId());
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                Currency currency = currencyRepository.findByBusinessIdAndActiveTrue(product.getBrand().getBusiness().getId());
                if (!currency.getName().equalsIgnoreCase("SO'M")) {
                    CurrentCource cource = currentCourceRepository.getByCurrencyId(currency.getId());
                    product.setSalePrice(product.getSalePrice() / cource.getCurrentCourse());
                    product.setBuyPrice(product.getBuyPrice() / cource.getCurrentCourse());
                }
                productAllByBarcode.add(product);
            }
        }
        if (productAllByBarcode.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }
        return new ApiResponse("FOUND", true, productAllByBarcode);
    }

    public ApiResponse getByCategory(UUID category_id, User user) {
        Set<Branch> branches = user.getBranches();
        List<Product> productList = new ArrayList<>();
        for (Branch branch : branches) {
            List<Product> all = productRepository.findAllByCategoryIdAndBranchIdAndActiveTrue(category_id, branch.getId());
            if (!all.isEmpty()) {
                for (Product product : all) {
                    Currency currency = currencyRepository.findByBusinessIdAndActiveTrue(product.getBrand().getBusiness().getId());
                    if (!currency.getName().equalsIgnoreCase("SO'M")) {
                        CurrentCource cource = currentCourceRepository.getByCurrencyId(currency.getId());
                        product.setSalePrice(product.getSalePrice() / cource.getCurrentCourse());
                        product.setBuyPrice(product.getBuyPrice() / cource.getCurrentCourse());
                    } else {
                        break;
                    }
                }
                productList.addAll(all);
            }
        }
        if (productList.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }
        return new ApiResponse("FOUND", true, productList);
    }

    public ApiResponse getByBrand(UUID brand_id, User user) {
        Set<Branch> branches = user.getBranches();
        List<Product> productList = new ArrayList<>();
        for (Branch branch : branches) {
            List<Product> all = productRepository.findAllByBrandIdAndBranchIdAndActiveTrue(brand_id, branch.getId());
            if (!all.isEmpty()) {
                for (Product product : all) {
                    Currency currency = currencyRepository.findByBusinessIdAndActiveTrue(product.getBrand().getBusiness().getId());
                    if (!currency.getName().equalsIgnoreCase("SO'M")) {
                        CurrentCource cource = currentCourceRepository.getByCurrencyId(currency.getId());
                        product.setSalePrice(product.getSalePrice() / cource.getCurrentCourse());
                        product.setBuyPrice(product.getBuyPrice() / cource.getCurrentCourse());
                    } else {
                        break;
                    }
                }
                productList.addAll(all);
            }
        }
        if (productList.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, productList);
    }

    public ApiResponse getByBranchAndBarcode(UUID branch_id, User user, ProductBarcodeDto barcodeDto) {
        Set<Branch> branches = user.getBranches();
        for (Branch branch : branches) {
            if (branch.getId().equals(branch_id)) {
                return new ApiResponse("BRANCH NOT FOUND OR NOT ALLOWED", false);
            }
        }
        List<Product> productList = productRepository.findAllByBranchIdAndBarcodeOrNameAndActiveTrue(branch_id, barcodeDto.getBarcode(), barcodeDto.getName());

        if (productList.isEmpty()) {
            return new ApiResponse("PRODUCT NOT FOUND", false);
        }
        return new ApiResponse("FOUND", true, productList);


    }

    public ApiResponse getByBranch(UUID branch_id) {
        ProductViewDto productViewDto=new ProductViewDto();

        List<Product> productList = productRepository.findAllByBranchIdAndActiveTrue(branch_id);
        if (productList.isEmpty()){
            return new ApiResponse("NOT FOUND",false);
        }else {
            for (Product product : productList) {
                productViewDto.setProductName(product.getName());
                productViewDto.setBrandName(product.getBrand().getName());
                productViewDto.setBuyPrice(product.getBuyPrice());
                productViewDto.setSalePrice(product.getSalePrice());
                productViewDto.setMinQuantity(product.getMinQuantity());
                productViewDto.setBranch(product.getBranch());
                productViewDto.setExpiredDate(product.getExpireDate());
                Warehouse warehouse = warehouseRepository.findByBranchIdAndProductId(branch_id, product.getId()).get();
                if (warehouse.getProduct().getId().equals(product.getId())) {
                    productViewDto.setAmount(warehouse.getAmount());
                }
            }
            return new ApiResponse("FOUND",true,productViewDto);
        }
    }

    public ApiResponse getByBusiness(UUID businessId) {
        List<ProductViewDto> productViewDtoList=new ArrayList<>();
        List<Product> productList = productRepository.findAllByBusiness_IdAndActiveTrue(businessId);
        if (productList.isEmpty()){
            return new ApiResponse("NOT FOUND",false);
        }else {
            for (Product product : productList) {
                ProductViewDto productViewDto = new ProductViewDto();
                productViewDto.setProductId(product.getId());
                productViewDto.setProductName(product.getName());
                productViewDto.setBrandName(product.getBrand().getName());
                productViewDto.setBarcode(product.getBarcode());
                productViewDto.setBuyPrice(product.getBuyPrice());
                productViewDto.setSalePrice(product.getSalePrice());
                productViewDto.setMinQuantity(product.getMinQuantity());
                productViewDto.setBranch(product.getBranch());
                productViewDto.setExpiredDate(product.getExpireDate());


                Optional<Measurement> optionalMeasurement = measurementRepository.findById(product.getMeasurement().getId());
                optionalMeasurement.ifPresent(measurement -> productViewDto.setMeasurementId(measurement.getName()));
                Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranch_BusinessIdAndProductId(businessId, product.getId());
                optionalWarehouse.ifPresent(warehouse -> productViewDto.setAmount(warehouse.getAmount()));
                productViewDtoList.add(productViewDto);
            }
            return new ApiResponse("FOUND", true, productViewDtoList);
        }
    }



    public ApiResponse deleteProducts(List<UUID> ids) {
        for (UUID id : ids) {
            Optional<Product> optional = productRepository.findById(id);
            if (optional.isEmpty()) {
                return new ApiResponse("not found", false);
            }
            Product product = optional.get();
            product.setActive(false);
            productRepository.save(product);
        }
        return new ApiResponse("DELETED", true);
    }
}
