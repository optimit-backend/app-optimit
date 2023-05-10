package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.entity.Currency;
import uz.pdp.springsecurity.enums.Type;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import java.util.*;


@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    private final BrandRepository brandRepository;

    private final CategoryRepository categoryRepository;

    private final MeasurementRepository measurementRepository;

    private final AttachmentRepository attachmentRepository;

    private final BranchRepository branchRepository;

    private final BusinessRepository businessRepository;

    private final ProductTypePriceRepository productTypePriceRepository;

    private final ProductTypeValueRepository productTypeValueRepository;

    private final WarehouseRepository warehouseRepository;

    private final ProductTypeComboRepository comboRepository;

    private final SubscriptionRepository subscriptionRepository;
    private final PurchaseProductRepository purchaseProductRepository;
    private final TradeProductRepository tradeProductRepository;
    private final ContentProductRepository contentProductRepository;
    private final ProductionRepository productionRepository;
    private final CurrencyRepository currencyRepository;
    private final FifoCalculationRepository fifoCalculationRepository;

    public ApiResponse addProduct(ProductDto productDto) {
        UUID businessId = productDto.getBusinessId();
        Optional<Business> optionalBusiness = businessRepository.findById(businessId);
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("not found business", false);
        }

        Optional<Subscription> optionalSubscription = subscriptionRepository.findByBusinessIdAndActiveTrue(businessId);
        if (optionalSubscription.isEmpty()) {
            return new ApiResponse("tariff aktiv emas", false);
        }

        Subscription subscription = optionalSubscription.get();

        List<Product> allProduct = productRepository.findAllByBusiness_IdAndActiveTrue(businessId);
        int size = allProduct.size();

        if (subscription.getTariff().getProductAmount() >= size || subscription.getTariff().getProductAmount() == 0) {
            Product product = new Product();
            product.setBusiness(optionalBusiness.get());
            return createOrEditProduct(product, productDto, false);
        }
        return new ApiResponse("You have to opened a sufficient branch according to the product", false);
    }

    public ApiResponse editProduct(UUID id, ProductDto productDto) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }
        return createOrEditProduct(optionalProduct.get(), productDto, true);
    }

    public ApiResponse createOrEditProduct(Product product, ProductDto productDto, boolean isUpdate) {

        UUID measurementId = productDto.getMeasurementId();
        List<UUID> branchId = productDto.getBranchId();

        Optional<Measurement> optionalMeasurement = measurementRepository.findById(measurementId);
        List<Branch> allBranch = branchRepository.findAllById(branchId);

        if (optionalMeasurement.isEmpty()) {
            return new ApiResponse("not found measurement", false);
        }
        if (allBranch.isEmpty()) {
            return new ApiResponse("not found branches", false);
        }

        product.setName(productDto.getName());
        product.setBranch(allBranch);
        product.setMeasurement(optionalMeasurement.get());

        product.setTax(productDto.getTax());
        product.setExpireDate(productDto.getExpireDate());
        product.setBarcode(productDto.getBarcode());
        product.setExpireDate(productDto.getExpireDate());
        product.setMinQuantity(productDto.getMinQuantity());
        product.setDueDate(productDto.getDueDate());
        product.setBuyDollar(productDto.isBuyDollar());
        product.setSaleDollar(productDto.isSaleDollar());
        product.setActive(true);

        if (productDto.getCategoryId() != null) {
            UUID categoryId = productDto.getCategoryId();
            Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
            optionalCategory.ifPresent(product::setCategory);

        }

        if (productDto.getChildCategoryId() != null) {
            UUID categoryId = productDto.getChildCategoryId();
            Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
            optionalCategory.ifPresent(product::setChildCategory);

        }

        if (productDto.getBrandId() != null) {
            UUID brandId = productDto.getBrandId();
            Optional<Brand> optionalBrand = brandRepository.findById(brandId);
            optionalBrand.ifPresent(product::setBrand);
        }

        if (productDto.getPhotoId() != null) {
            Optional<Attachment> optionalAttachment = attachmentRepository.findById(productDto.getPhotoId());
            optionalAttachment.ifPresent(product::setPhoto);
        }

        Business business = allBranch.get(0).getBusiness();
        Optional<Currency> optionalCurrency = currencyRepository.findByBusinessId(business.getId());
        Currency currency;
        if (optionalCurrency.isPresent()){
            currency = optionalCurrency.get();
        }else {
            Optional<Currency> optional = currencyRepository.findFirstByCourseIsNotNullOrderByUpdateAtDesc();
            currency = optional.orElseGet(() -> currencyRepository.save(new Currency(
                    business,
                    11400
            )));
        }

        if (productDto.getType().equals(Type.SINGLE.name())) {
            return addProductTypeSingleDto(productDto, product, isUpdate, currency);
        } else if (productDto.getType().equals(Type.MANY.name())) {
            return addProductTypeManyDto(productDto, product, currency);
        } else if (productDto.getType().equals(Type.COMBO.name())) {
            return addProductTypeComboDto(productDto, product, isUpdate, currency);
        } else {
            return new ApiResponse("no such type exists", false);
        }
    }

    private ApiResponse addProductTypeComboDto(ProductDto productDto, Product product, boolean isUpdate, Currency currency) {

        product.setType(Type.COMBO);
        product.setBuyPrice(productDto.getBuyPrice());
        product.setSalePrice(productDto.getSalePrice());
        product.setBuyPriceDollar(Math.round(productDto.getBuyPrice() / currency.getCourse() * 100) / 100.);
        product.setSalePriceDollar(Math.round(productDto.getSalePrice() / currency.getCourse() * 100) / 100.);
        product.setProfitPercent(productDto.getProfitPercent());
        if (productDto.getBarcode() != null && !productDto.getBarcode().isBlank()) {
            if (isUpdate) {
                if (productRepository.existsByBarcodeAndBusinessIdAndIdIsNotAndActiveTrue(productDto.getBarcode(), product.getBusiness().getId(), product.getId()))
                    return new ApiResponse("product with the barcode is already exist");
            } else {
                if (productRepository.existsByBarcodeAndBusinessIdAndActiveTrue(productDto.getBarcode(), product.getBusiness().getId()))
                    return new ApiResponse("product with the barcode is already exist");
            }
            product.setBarcode(productDto.getBarcode());
        } else {
            product.setBarcode(generateBarcode(product.getBusiness().getId(), product.getName(), product.getId(), isUpdate));
        }
        Product saveProduct = productRepository.save(product);

        List<ProductTypeComboDto> productTypeComboDtoList = productDto.getProductTypeComboDtoList();

        List<ProductTypeCombo> productTypeComboList = new ArrayList<>();

        for (ProductTypeComboDto productTypeComboDto : productTypeComboDtoList) {
            Optional<Product> optionalProduct = productRepository.findById(productTypeComboDto.getContentProductId());
            Optional<ProductTypePrice> optionalProductTypePrice = productTypePriceRepository.findById(productTypeComboDto.getContentProductId());
            if (isUpdate && productTypeComboDto.getComboId() != null) {
                Optional<ProductTypeCombo> comboOptional = comboRepository.findById(productTypeComboDto.getComboId());
                if (comboOptional.isEmpty()) {
                    return new ApiResponse("not found combo product", false);
                }
                ProductTypeCombo productTypeCombo = comboOptional.get();
                productTypeCombo.setMainProduct(saveProduct);

                optionalProduct.ifPresent(productTypeCombo::setContentProduct);
                optionalProductTypePrice.ifPresent(productTypeCombo::setContentProductTypePrice);

                productTypeCombo.setAmount(productTypeComboDto.getAmount());
                productTypeCombo.setBuyPrice(productTypeComboDto.getBuyPrice());
                productTypeCombo.setSalePrice(productTypeComboDto.getSalePrice());
//                    productTypeCombo.setMeasurement(saveProduct.getMeasurement());
                productTypeComboList.add(productTypeCombo);
            } else {
                ProductTypeCombo productTypeCombo = new ProductTypeCombo();
                productTypeCombo.setMainProduct(saveProduct);
//                productTypeCombo.setMeasurement(saveProduct.getMeasurement());
                optionalProduct.ifPresent(productTypeCombo::setContentProduct);
                optionalProductTypePrice.ifPresent(productTypeCombo::setContentProductTypePrice);

                productTypeCombo.setAmount(productTypeComboDto.getAmount());
                productTypeCombo.setBuyPrice(productTypeComboDto.getBuyPrice());
                productTypeCombo.setSalePrice(productTypeComboDto.getSalePrice());
                productTypeComboList.add(productTypeCombo);
            }
        }
        comboRepository.saveAll(productTypeComboList);
        return new ApiResponse("successfully saved", true);
    }

    private ApiResponse addProductTypeSingleDto(ProductDto productDto, Product product, boolean isUpdate, Currency currency) {
        product.setType(Type.SINGLE);
        product.setBuyPrice(productDto.getBuyPrice());
        product.setSalePrice(productDto.getSalePrice());
        product.setBuyPriceDollar(Math.round(productDto.getBuyPrice() / currency.getCourse() * 100) / 100.);
        product.setSalePriceDollar(Math.round(productDto.getSalePrice() / currency.getCourse() * 100) / 100.);
        product.setProfitPercent(productDto.getProfitPercent());
        if (productDto.getBarcode() != null && !productDto.getBarcode().isBlank()) {
            if (isUpdate) {
                if (productRepository.existsByBarcodeAndBusinessIdAndIdIsNotAndActiveTrue(productDto.getBarcode(), product.getBusiness().getId(), product.getId())
                        || productTypePriceRepository.existsByBarcodeAndProduct_BusinessId(productDto.getBarcode(), product.getBusiness().getId()))
                    return new ApiResponse("product with the barcode is already exist");
            } else {
                if (productRepository.existsByBarcodeAndBusinessIdAndActiveTrue(productDto.getBarcode(), product.getBusiness().getId())
                    || productTypePriceRepository.existsByBarcodeAndProduct_BusinessId(productDto.getBarcode(), product.getBusiness().getId()))
                    return new ApiResponse("product with the barcode is already exist");
            }
            product.setBarcode(productDto.getBarcode());
        } else {
            product.setBarcode(generateBarcode(product.getBusiness().getId(), product.getName(), product.getId(), isUpdate));
        }

        productRepository.save(product);

        return new ApiResponse("successfully added", true);
    }


    private ApiResponse addProductTypeManyDto(ProductDto productDto, Product product, Currency currency) {
        product.setType(Type.MANY);

        Product saveProduct = productRepository.save(product);
//        List<ProductTypePrice> productTypePriceList = new ArrayList<>();

        for (ProductTypePricePostDto typePricePostDto : productDto.getProductTypePricePostDtoList()) {
            Optional<ProductTypeValue> optionalProductTypeValue = productTypeValueRepository.findById(typePricePostDto.getProductTypeValueId());
            if (optionalProductTypeValue.isEmpty()) return new ApiResponse("not found product type value", false);
            if (typePricePostDto.getProductTypePriceId() != null) {
                Optional<ProductTypePrice> typePriceOptional = productTypePriceRepository.findById(typePricePostDto.getProductTypePriceId());
                if (typePriceOptional.isEmpty()) {
                    return new ApiResponse("not found product type many id", false);
                }

                ProductTypeValue productTypeValue = optionalProductTypeValue.get();
                ProductTypePrice productTypePrice = typePriceOptional.get();
                productTypePrice.setProduct(saveProduct);
                productTypePrice.setName(product.getName() + "( " + productTypeValue.getProductType().getName() + " - " + productTypeValue.getName() + " )");
                productTypePrice.setProductTypeValue(optionalProductTypeValue.get());
                productTypePrice.setBuyPrice(typePricePostDto.getBuyPrice());
                productTypePrice.setSalePrice(typePricePostDto.getSalePrice());
                productTypePrice.setBuyPriceDollar(Math.round(typePricePostDto.getBuyPrice() / currency.getCourse() * 100) / 100.);
                productTypePrice.setSalePriceDollar(Math.round(typePricePostDto.getSalePrice() / currency.getCourse() * 100) / 100.);
                productTypePrice.setProfitPercent(typePricePostDto.getProfitPercent());
                if (typePricePostDto.getPhotoId() != null){
                    Optional<Attachment> optionalAttachment = attachmentRepository.findById(typePricePostDto.getPhotoId());
                    optionalAttachment.ifPresent(productTypePrice::setPhoto);
                }
                if (typePricePostDto.getBarcode() != null && !typePricePostDto.getBarcode().isBlank()) {
                    if (productTypePriceRepository.existsByBarcodeAndProduct_BusinessIdAndIdIsNot(typePricePostDto.getBarcode(), product.getBusiness().getId(), productTypePrice.getId())
                            || productRepository.existsByBarcodeAndBusinessIdAndActiveTrue(typePricePostDto.getBarcode(), product.getBusiness().getId())) {
                        productTypePrice.setBarcode(generateBarcode(saveProduct.getBusiness().getId(), saveProduct.getName(), productTypePrice.getId(), false));
                    }else {
                        productTypePrice.setBarcode(typePricePostDto.getBarcode());
                    }
                } else {
                    productTypePrice.setBarcode(generateBarcode(saveProduct.getBusiness().getId(), saveProduct.getName(), productTypePrice.getId(), false));
                }
                productTypePriceRepository.save(productTypePrice);
//                productTypePriceList.add(productTypePrice);
            } else {
                ProductTypeValue productTypeValue = optionalProductTypeValue.get();
                ProductTypePrice productTypePrice = new ProductTypePrice();
                productTypePrice.setProduct(saveProduct);
                productTypePrice.setName(product.getName() + "( " + productTypeValue.getProductType().getName() + " - " + productTypeValue.getName() + " )");
                productTypePrice.setProductTypeValue(optionalProductTypeValue.get());
                productTypePrice.setBuyPrice(typePricePostDto.getBuyPrice());
                productTypePrice.setSalePrice(typePricePostDto.getSalePrice());
                productTypePrice.setBuyPriceDollar(Math.round(typePricePostDto.getBuyPrice() / currency.getCourse() * 100) / 100.);
                productTypePrice.setSalePriceDollar(Math.round(typePricePostDto.getSalePrice() / currency.getCourse() * 100) / 100.);
                productTypePrice.setProfitPercent(typePricePostDto.getProfitPercent());
                if (typePricePostDto.getPhotoId() != null){
                    Optional<Attachment> optionalAttachment = attachmentRepository.findById(typePricePostDto.getPhotoId());
                    optionalAttachment.ifPresent(productTypePrice::setPhoto);
                }
                if (typePricePostDto.getBarcode() != null && !typePricePostDto.getBarcode().isBlank()) {
                    if (productTypePriceRepository.existsByBarcodeAndProduct_BusinessId(typePricePostDto.getBarcode(), product.getBusiness().getId())
                        || productRepository.existsByBarcodeAndBusinessIdAndActiveTrue(typePricePostDto.getBarcode(), product.getBusiness().getId())) {
                        productTypePrice.setBarcode(generateBarcode(saveProduct.getBusiness().getId(), saveProduct.getName(), productTypePrice.getId(), false));
                    }else {
                        productTypePrice.setBarcode(typePricePostDto.getBarcode());
                    }
                } else {
                    productTypePrice.setBarcode(generateBarcode(saveProduct.getBusiness().getId(), saveProduct.getName(), productTypePrice.getId(), false));
                }
                productTypePriceRepository.save(productTypePrice);
//                productTypePriceList.add(productTypePrice);
            }
        }

//        productTypePriceRepository.saveAll(productTypePriceList);
        return new ApiResponse("successfully saved", true);
    }

    private String generateBarcode(UUID businessId, String productName, UUID productId, boolean isUpdate) {
        String name = productName.toLowerCase();
        StringBuilder str = new StringBuilder(String.valueOf(System.currentTimeMillis()));
        str.append(name.charAt(0));
        str.reverse();
        String barcode = str.substring(0, 9);
        if (isUpdate) {
            if (productRepository.existsByBarcodeAndBusinessIdAndIdIsNotAndActiveTrue(barcode, businessId, productId) || productTypePriceRepository.existsByBarcodeAndProduct_BusinessIdAndIdIsNot(barcode, businessId, productId))
                return generateBarcode(businessId, productName, productId, isUpdate);
        } else {
            if (productRepository.existsByBarcodeAndBusinessIdAndActiveTrue(barcode, businessId) || productTypePriceRepository.existsByBarcodeAndProduct_BusinessId(barcode, businessId))
                return generateBarcode(businessId, productName, productId, isUpdate);
        }
        return barcode;
    }

    public ApiResponse getAll(User user) {
        Set<Branch> branches = user.getBranches();
        List<Product> productList = new ArrayList<>();
        for (Branch branch : branches) {
            List<Product> all = productRepository.findAllByBranchIdAndActiveIsTrue(branch.getId());
            if (!all.isEmpty()) {
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
                return getProductHelper(branch, optionalProduct.get());
            }
        }
        return new ApiResponse("NOT FOUND", false);
    }

    public ApiResponse getProductHelper(Branch branch, Product product) {
        ProductGetDto productGetDto = new ProductGetDto(product);

        if (product.getType().name().equals(Type.SINGLE.name())) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch.getId(), product.getId());
            product.setQuantity(optionalWarehouse.map(Warehouse::getAmount).orElse(0d));
            return new ApiResponse("productGetDto", true, productGetDto);
        } else if (product.getType().name().equals(Type.MANY.name())) {
            List<ProductTypePriceGetDto> productTypePriceGetDtoList = new ArrayList<>();
            List<ProductTypePrice> allByProductId = productTypePriceRepository.findAllByProductId(product.getId());
            for (ProductTypePrice productTypePrice : allByProductId) {
                ProductTypePriceGetDto productTypePriceGetDto = new ProductTypePriceGetDto();

                productTypePriceGetDto.setProductTypePriceId(productTypePrice.getId());
                productTypePriceGetDto.setProductTypeName(productTypePrice.getProductTypeValue().getProductType().getName());
                productTypePriceGetDto.setProductTypeValueName(productTypePrice.getProductTypeValue().getName());
                productTypePriceGetDto.setBarcode(productTypePrice.getBarcode());
                productTypePriceGetDto.setProfitPercent(productTypePrice.getProfitPercent());
                productTypePriceGetDto.setBuyPrice(productTypePrice.getBuyPrice());
                productTypePriceGetDto.setSalePrice(productTypePrice.getSalePrice());
                productTypePriceGetDto.setBuyPriceDollar(productTypePrice.getBuyPriceDollar());
                productTypePriceGetDto.setSalePriceDollar(productTypePrice.getSalePriceDollar());
                productTypePriceGetDto.setProductTypeValueNameId(productTypePrice.getProductTypeValue().getId());
                if (productTypePrice.getPhoto() != null)productTypePriceGetDto.setPhotoId(productTypePrice.getPhoto().getId());
                Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branch.getId(), productTypePrice.getId());
                productTypePriceGetDto.setQuantity(optionalWarehouse.map(Warehouse::getAmount).orElse(0d));
                productTypePriceGetDtoList.add(productTypePriceGetDto);
            }
            productGetDto.setProductTypePriceGetDtoList(productTypePriceGetDtoList);
            return new ApiResponse("productGetDto", true, productGetDto);

        } else {

            List<ProductTypeComboGetDto> comboGetDtoList = new ArrayList<>();
            List<ProductTypeCombo> allComboProduct = comboRepository.findAllByMainProductId(product.getId());
            for (ProductTypeCombo combo : allComboProduct) {
                ProductTypeComboGetDto comboGetDto = new ProductTypeComboGetDto();
                comboGetDto.setComboId(combo.getId());
                if (combo.getContentProduct() != null) {
                    comboGetDto.setContentProduct(combo.getContentProduct());
                } else {
                    comboGetDto.setContentProductTypePrice(combo.getContentProductTypePrice());
                }
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
        Branch branchGet = null;
        for (Branch branch : branches) {
            Optional<Product> optionalProduct = productRepository.findAllByBarcodeAndBranchIdAndActiveTrue(barcode, branch.getId());
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                productAllByBarcode.add(product);
                branchGet = branch;
            }
        }


        List<ProductViewDto> viewDtos = new ArrayList<>();
        for (Product product : productAllByBarcode) {
            ProductViewDto productViewDto = new ProductViewDto();
            productViewDto.setProductId(product.getId());
            productViewDto.setProductName(product.getName());
            if (product.getBrand() != null)
                productViewDto.setBrandName(product.getBrand().getName());
            productViewDto.setBuyPrice(product.getBuyPrice());
            productViewDto.setSalePrice(product.getSalePrice());
            productViewDto.setMinQuantity(product.getMinQuantity());
            productViewDto.setBranch(product.getBranch());
            productViewDto.setExpiredDate(product.getExpireDate());

            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branchGet.getId(), product.getId());
            if (optionalWarehouse.isPresent()) {
                Warehouse warehouse = optionalWarehouse.get();
                if (warehouse.getProduct().getId().equals(product.getId())) {
                    productViewDto.setAmount(warehouse.getAmount());
                }
            }
            viewDtos.add(productViewDto);
        }

        if (viewDtos.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }
        return new ApiResponse("FOUND", true, viewDtos);
    }

    public ApiResponse getByCategory(UUID category_id, User user) {
        Set<Branch> branches = user.getBranches();
        List<Product> productList = new ArrayList<>();
        for (Branch branch : branches) {
            List<Product> all = productRepository.findAllByCategoryIdAndBranchIdAndActiveTrue(category_id, branch.getId());
            if (!all.isEmpty()) {
                productList.addAll(all);
            }
        }

        List<ProductViewDto> viewDtos = new ArrayList<>();
        for (Product product : productList) {
            ProductViewDto productViewDto = new ProductViewDto();
            productViewDto.setProductId(product.getId());
            productViewDto.setProductName(product.getName());
            if (product.getBrand() != null)
                productViewDto.setBrandName(product.getBrand().getName());
            productViewDto.setBuyPrice(product.getBuyPrice());
            productViewDto.setSalePrice(product.getSalePrice());
            productViewDto.setMinQuantity(product.getMinQuantity());
            productViewDto.setBranch(product.getBranch());
            productViewDto.setExpiredDate(product.getExpireDate());
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByProduct_Id(product.getId());
            if (optionalWarehouse.isPresent()) {
                Warehouse warehouse = optionalWarehouse.get();
                if (warehouse.getProduct().getId().equals(product.getId())) {
                    productViewDto.setAmount(warehouse.getAmount());
                }
            }
            viewDtos.add(productViewDto);
        }


        if (viewDtos.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }
        return new ApiResponse("FOUND", true, viewDtos);
    }

    public ApiResponse getByBrand(UUID brand_id) {
        List<ProductViewDto> productViewDtos = new ArrayList<>();
        List<Product> allProductByBrand = productRepository.findAllByBrandIdAndActiveIsTrue(brand_id);
        getProductMethod(productViewDtos, allProductByBrand, null);

        return new ApiResponse("FOUND", true, productViewDtos);
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
        return getProductByBranch(branch_id);

    }

    public ApiResponse getByBranchForSearch(UUID branch_id) {

        List<ProductGetForPurchaseDto> getForPurchaseDtoList = new ArrayList<>();
        List<Product> productList = productRepository.findAllByBranchIdAndActiveIsTrue(branch_id);
        if (productList.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        } else {
            for (Product product : productList) {
                if (product.getType().equals(Type.MANY)) {
                    List<ProductTypePrice> productTypePriceList = productTypePriceRepository.findAllByProductId(product.getId());
                    for (ProductTypePrice productTypePrice : productTypePriceList) {
                        ProductGetForPurchaseDto getForPurchaseDto = new ProductGetForPurchaseDto();
                        getForPurchaseDto.setProductTypePriceId(productTypePrice.getId());
                        getForPurchaseDto.setType(Type.MANY.name());
                        getForPurchaseDto.setName(productTypePrice.getName());
                        getForPurchaseDto.setBarcode(productTypePrice.getBarcode());
                        getForPurchaseDto.setBuyPrice(productTypePrice.getBuyPrice());
                        getForPurchaseDto.setSalePrice(productTypePrice.getSalePrice());
                        getForPurchaseDto.setBuyDollar(productTypePrice.getProduct().isBuyDollar());
                        getForPurchaseDto.setSaleDollar(productTypePrice.getProduct().isSaleDollar());
                        getForPurchaseDto.setBuyPriceDollar(productTypePrice.getSalePriceDollar());
                        getForPurchaseDto.setSalePriceDollar(productTypePrice.getSalePriceDollar());
                        getForPurchaseDto.setProfitPercent(productTypePrice.getProfitPercent());
                        getForPurchaseDto.setMinQuantity(product.getMinQuantity());
                        getForPurchaseDto.setExpiredDate(product.getExpireDate());
                        getForPurchaseDto.setMeasurementName(product.getMeasurement().getName());
                        if (product.getMeasurement() != null)
                            getForPurchaseDto.setMeasurementName(product.getMeasurement().getName());
                        if (product.getBrand() != null) getForPurchaseDto.setBrandName(product.getBrand().getName());
                        if (productTypePrice.getPhoto() != null)
                            getForPurchaseDto.setPhotoId(productTypePrice.getPhoto().getId());
                        Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branch_id, productTypePrice.getId());
                        getForPurchaseDto.setAmount(optionalWarehouse.map(Warehouse::getAmount).orElse(0d));
                        getForPurchaseDtoList.add(getForPurchaseDto);
                    }
                } else {
                    ProductGetForPurchaseDto getForPurchaseDto = new ProductGetForPurchaseDto();
                    getForPurchaseDto.setProductId(product.getId());
                    getForPurchaseDto.setType(product.getType().name());
                    getForPurchaseDto.setName(product.getName());
                    getForPurchaseDto.setBarcode(product.getBarcode());
                    getForPurchaseDto.setBuyPrice(product.getBuyPrice());
                    getForPurchaseDto.setSalePrice(product.getSalePrice());
                    getForPurchaseDto.setBuyDollar(product.isBuyDollar());
                    getForPurchaseDto.setSaleDollar(product.isSaleDollar());
                    getForPurchaseDto.setBuyPriceDollar(product.getSalePriceDollar());
                    getForPurchaseDto.setSalePriceDollar(product.getSalePriceDollar());
                    getForPurchaseDto.setMinQuantity(product.getMinQuantity());
                    getForPurchaseDto.setExpiredDate(product.getExpireDate());
                    if (product.getMeasurement() != null)
                        getForPurchaseDto.setMeasurementName(product.getMeasurement().getName());
                    if (product.getBrand() != null) getForPurchaseDto.setBrandName(product.getBrand().getName());
                    if (product.getPhoto() != null) getForPurchaseDto.setPhotoId(product.getPhoto().getId());
                    Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch_id, product.getId());
                    getForPurchaseDto.setAmount(optionalWarehouse.map(Warehouse::getAmount).orElse(0d));
                    getForPurchaseDtoList.add(getForPurchaseDto);
                }
            }
            return new ApiResponse("FOUND", true, getForPurchaseDtoList);
        }
    }

    public ApiResponse getByBusiness(UUID businessId, UUID branch_id, UUID brand_id,UUID categoryId) {
        List<ProductViewDto> productViewDtoList = new ArrayList<>();
        List<Product> productList = new ArrayList<>();

        boolean checkingBranch = false;
        boolean checkingBrand = false;
        boolean checkingCategory = false;
        boolean checkingBusiness = false;

        if (categoryId!=null){
            checkingCategory = true;
        }
        if (brand_id!=null){
            checkingBrand = true;
        }
        if (branch_id!=null){
            checkingBranch = true;
        }
        if (businessId!=null){
            checkingBusiness = true;
        }
        if (checkingBranch){
            checkingBusiness = false;
        }

        if (checkingCategory && checkingBrand && checkingBranch){
            productList = productRepository.findAllByBrandIdAndCategoryIdAndBranchIdAndActiveTrue(brand_id,categoryId,branch_id);
        } else if (checkingBrand && checkingBranch) {
            productList = productRepository.findAllByBrandIdAndBranchIdAndActiveTrue(brand_id,branch_id);
        } else if (checkingCategory && checkingBranch) {
            productList = productRepository.findAllByCategoryIdAndBranchIdAndActiveTrue(categoryId,branch_id);
        } else if (checkingCategory && checkingBrand && checkingBusiness) {
            productList = productRepository.findAllByBrandIdAndCategoryIdAndBusinessIdAndActiveTrue(brand_id,categoryId,businessId);
        } else if (checkingBrand && checkingBusiness) {
            productList = productRepository.findAllByBrandIdAndBusinessIdAndActiveTrue(brand_id,businessId);
        } else if (checkingCategory && checkingBusiness) {
            productList = productRepository.findAllByCategoryIdAndBusinessIdAndActiveTrue(categoryId,businessId);
        } else if (checkingBusiness) {
            productList = productRepository.findAllByBusiness_IdAndActiveTrue(businessId);
        } else if (checkingBranch) {
            productList = productRepository.findAllByBranchIdAndActiveTrue(branch_id);
        }
        if (checkingBranch){

            getProductMethod(productViewDtoList, productList, branch_id);
        }else {
            getProductMethod(productViewDtoList, productList, null);
        }


        if (productList.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }

        return new ApiResponse("FOUND", true, productViewDtoList);
    }


    public ApiResponse getByBranchProduct(UUID branchId) {
        return getProductByBranch(branchId);
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

    private ApiResponse getProductByBranch(UUID branchId) {
        List<ProductViewDto> productViewDtoList = new ArrayList<>();
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);

        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found");
        }
        List<Product> productList = productRepository.findAllByBranchIdAndActiveIsTrue(branchId);
        getProductMethod(productViewDtoList, productList, branchId);
        return new ApiResponse("FOUND", true, productViewDtoList);
    }

    private void getProductMethod(List<ProductViewDto> productViewDtoList, List<Product> productList, UUID branchId) {
        for (Product product : productList) {
            ProductViewDto productViewDto = new ProductViewDto();
            productViewDto.setProductId(product.getId());
            productViewDto.setProductName(product.getName());
            if (product.getBrand() != null)
                productViewDto.setBrandName(product.getBrand().getName());
            productViewDto.setBarcode(product.getBarcode());

            productViewDto.setMinQuantity(product.getMinQuantity());
            productViewDto.setBuyDollar(product.isBuyDollar());
            productViewDto.setSaleDollar(product.isSaleDollar());
            productViewDto.setBranch(product.getBranch());
            productViewDto.setExpiredDate(product.getExpireDate());
            if (product.getPhoto() != null) {
                productViewDto.setPhotoId(product.getPhoto().getId());
            }
            Optional<Measurement> optionalMeasurement = measurementRepository.findById(product.getMeasurement().getId());
            optionalMeasurement.ifPresent(measurement -> productViewDto.setMeasurementId(measurement.getName()));

            if (product.getType().equals(Type.MANY)) {
                double total = 0;
                List<ProductTypePrice> typePriceRepositoryAllByProductId = productTypePriceRepository.findAllByProductId(product.getId());
                for (ProductTypePrice productTypePrice : typePriceRepositoryAllByProductId) {
                    List<Warehouse> allByByProductId;
                    if (branchId != null) {
                        allByByProductId = warehouseRepository.findAllByProductTypePrice_IdAndBranch_Id(productTypePrice.getId(), branchId);
                    } else {
                        allByByProductId = warehouseRepository.findAllByProductTypePrice_Id(productTypePrice.getId());
                    }

                    if (allByByProductId.isEmpty()) {
                        productViewDto.setAmount(0);
                    }
                    double totalAmount = 0;
                    for (Warehouse warehouse : allByByProductId) {
                        totalAmount += warehouse.getAmount();
                    }
                    total += totalAmount;
                    productViewDto.setBuyPrice(productTypePrice.getBuyPrice());
                    productViewDto.setSalePrice(productTypePrice.getSalePrice());
                    productViewDto.setBuyPriceDollar(productTypePrice.getBuyPriceDollar());
                    productViewDto.setSalePriceDollar(productTypePrice.getSalePriceDollar());
                }
                productViewDto.setAmount(total);
            } else {
                List<Warehouse> allByByProductId;
                if (branchId != null) {
                    allByByProductId = warehouseRepository.findAllByProduct_IdAndBranch_Id(product.getId(), branchId);
                } else {
                    allByByProductId = warehouseRepository.findAllByProduct_Id(product.getId());
                }
                if (allByByProductId.isEmpty()) {
                    productViewDto.setAmount(0);
                } else {
                    double totalAmount = 0;
                    for (Warehouse warehouse : allByByProductId) {
                        totalAmount += warehouse.getAmount();
                    }
                    productViewDto.setAmount(totalAmount);
                }
                productViewDto.setBuyPrice(product.getBuyPrice());
                productViewDto.setSalePrice(product.getSalePrice());
                productViewDto.setBuyPriceDollar(product.getBuyPriceDollar());
                productViewDto.setSalePriceDollar(product.getSalePriceDollar());
                if (product.getCategory()!=null){
                    productViewDto.setCategory(product.getCategory().getName());
                }
            }

            productViewDtoList.add(productViewDto);
        }
    }

    public ApiResponse getPurchaseProduct(UUID branchId, UUID productId, int page, int size) {
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NOT FOUND", false);
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseProduct> purchaseProductPage;
        if (productRepository.existsById(productId)) {
            purchaseProductPage = purchaseProductRepository.findAllByPurchase_BranchIdAndProductIdOrderByCreatedAtDesc(branchId, productId, pageable);
        } else if (productTypePriceRepository.existsById(productId)) {
            purchaseProductPage = purchaseProductRepository.findAllByPurchase_BranchIdAndProductTypePriceIdOrderByCreatedAtDesc(branchId, productId, pageable);
        }else {
            return new ApiResponse("PRODUCT NOT FOUND", false);
        }
        if (purchaseProductPage.isEmpty()) return new ApiResponse("LIST EMPTY", false);

        List<ProductHistoryDto> productHistoryDtoList = new ArrayList<>();
        for (PurchaseProduct purchaseProduct : purchaseProductPage.getContent()) {
            productHistoryDtoList.add(new ProductHistoryDto(
                    purchaseProduct.getProduct() != null? purchaseProduct.getProduct().getName():purchaseProduct.getProductTypePrice().getName(),
                    purchaseProduct.getPurchasedQuantity(),
                    purchaseProduct.getTotalSum(),
                    purchaseProduct.getCreatedAt(),
                    purchaseProduct.getPurchase().getSeller().getFirstName(),
                    purchaseProduct.getPurchase().getSeller().getLastName(),
                    purchaseProduct.getPurchase().getSupplier().getName()
            ));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("productHistoryDtoList", productHistoryDtoList);
        response.put("currentPage", purchaseProductPage.getNumber());
        response.put("totalItem", purchaseProductPage.getTotalElements());
        response.put("totalPage", purchaseProductPage.getTotalPages());
        return new ApiResponse("all", true, response);
    }

    public ApiResponse getProductionProduct(UUID branchId, UUID productId, int page, int size) {
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NOT FOUND", false);
        Pageable pageable = PageRequest.of(page, size);
        Page<FifoCalculation> fifoCalculationPage;
        if (productRepository.existsById(productId)) {
            fifoCalculationPage = fifoCalculationRepository.findAllByBranchIdAndProductIdAndProductionIsNotNullOrderByCreatedAtDesc(branchId, productId, pageable);
//            fifoCalculationPage = productionRepository.findAllByBranchIdAndProductIdOrderByCreatedAtDesc(branchId, productId, pageable);
        } else if (productTypePriceRepository.existsById(productId)) {
            fifoCalculationPage = fifoCalculationRepository.findAllByBranchIdAndProductTypePriceIdAndProductionIsNotNullOrderByCreatedAtDesc(branchId, productId, pageable);
//            fifoCalculationPage = productionRepository.findAllByBranchIdAndProductTypePriceIdOrderByCreatedAtDesc(branchId, productId, pageable);
        }else {
            return new ApiResponse("PRODUCT NOT FOUND", false);
        }
        if (fifoCalculationPage.isEmpty()) return new ApiResponse("LIST EMPTY", false);

        List<ProductHistoryDto> productHistoryDtoList = new ArrayList<>();
        for (FifoCalculation fifoCalculation : fifoCalculationPage.getContent()) {
            ProductHistoryDto productHistoryDto = new ProductHistoryDto(
                    fifoCalculation.getProduct() != null ? fifoCalculation.getProduct().getName() : fifoCalculation.getProductTypePrice().getName(),
                    fifoCalculation.getPurchasedAmount(),
                    0,
                    fifoCalculation.getCreatedAt()
            );
            if (fifoCalculation.getProduct() != null) {
                if (fifoCalculation.getProduct().equals(fifoCalculation.getProduction().getProduct()))
                    productHistoryDto.setSumma(fifoCalculation.getProduction().getTotalPrice());
                else
                    productHistoryDto.setSumma(fifoCalculation.getProduct().getSalePrice() * fifoCalculation.getPurchasedAmount());
            } else {
                if (fifoCalculation.getProductTypePrice().equals(fifoCalculation.getProduction().getProductTypePrice()))
                    productHistoryDto.setSumma(fifoCalculation.getProduction().getTotalPrice());
                else
                    productHistoryDto.setSumma(fifoCalculation.getProductTypePrice().getSalePrice() * fifoCalculation.getPurchasedAmount());
            }
            productHistoryDtoList.add(productHistoryDto);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("productHistoryDtoList", productHistoryDtoList);
        response.put("currentPage", fifoCalculationPage.getNumber());
        response.put("totalItem", fifoCalculationPage.getTotalElements());
        response.put("totalPage", fifoCalculationPage.getTotalPages());
        return new ApiResponse("all", true, response);
    }

    public ApiResponse getTradeProduct(UUID branchId, UUID productId, int page, int size) {
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NOT FOUND", false);
        Pageable pageable = PageRequest.of(page, size);
        Page<TradeProduct> tradeProductPage;
        if (productRepository.existsById(productId)) {
            tradeProductPage = tradeProductRepository.findAllByTrade_BranchIdAndProductIdOrderByCreatedAtDesc(branchId, productId, pageable);
        } else if (productTypePriceRepository.existsById(productId)) {
            tradeProductPage = tradeProductRepository.findAllByTrade_BranchIdAndProductTypePriceIdOrderByCreatedAtDesc(branchId, productId, pageable);
        }else {
            return new ApiResponse("PRODUCT NOT FOUND", false);
        }
        if (tradeProductPage.isEmpty()) return new ApiResponse("LIST EMPTY", false);

        List<ProductHistoryDto> productHistoryDtoList = new ArrayList<>();
        for (TradeProduct tradeProduct : tradeProductPage.getContent()) {
            productHistoryDtoList.add(new ProductHistoryDto(
                    tradeProduct.getProduct() != null? tradeProduct.getProduct().getName():tradeProduct.getProductTypePrice().getName(),
                    tradeProduct.getTradedQuantity(),
                    tradeProduct.getTotalSalePrice(),
                    tradeProduct.getCreatedAt(),
                    tradeProduct.getTrade().getTrader().getFirstName(),
                    tradeProduct.getTrade().getTrader().getLastName(),
                    tradeProduct.getTrade().getCustomer()==null?"":tradeProduct.getTrade().getCustomer().getName()
            ));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("productHistoryDtoList", productHistoryDtoList);
        response.put("currentPage", tradeProductPage.getNumber());
        response.put("totalItem", tradeProductPage.getTotalElements());
        response.put("totalPage", tradeProductPage.getTotalPages());
        return new ApiResponse("all", true, response);
    }

    public ApiResponse getContentProduct(UUID branchId, UUID productId, int page, int size) {
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NOT FOUND", false);
        Pageable pageable = PageRequest.of(page, size);
        Page<ContentProduct> contentProductPage;
        if (productRepository.existsById(productId)) {
            contentProductPage = contentProductRepository.findAllByProduction_BranchIdAndProductIdAndProductionIsNotNullAndByProductIsFalseAndLossProductIsFalseOrderByCreatedAtDesc(branchId, productId, pageable);
        } else if (productTypePriceRepository.existsById(productId)) {
            contentProductPage = contentProductRepository.findAllByProduction_BranchIdAndProductTypePriceIdAndProductionIsNotNullAndByProductIsFalseAndLossProductIsFalseOrderByCreatedAtDesc(branchId, productId, pageable);
        }else {
            return new ApiResponse("PRODUCT NOT FOUND", false);
        }
        if (contentProductPage.isEmpty()) return new ApiResponse("LIST EMPTY", false);

        List<ProductHistoryDto> productHistoryDtoList = new ArrayList<>();
        for (ContentProduct contentProduct : contentProductPage.getContent()) {
            productHistoryDtoList.add(new ProductHistoryDto(
                    contentProduct.getProduct() != null? contentProduct.getProduct().getName():contentProduct.getProductTypePrice().getName(),
                    contentProduct.getQuantity(),
                    contentProduct.getTotalPrice(),
                    contentProduct.getCreatedAt()
            ));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("productHistoryDtoList", productHistoryDtoList);
        response.put("currentPage", contentProductPage.getNumber());
        response.put("totalItem", contentProductPage.getTotalElements());
        response.put("totalPage", contentProductPage.getTotalPages());
        return new ApiResponse("all", true, response);
    }

    public void editPriceAccordingToDollar(UUID businessId, double course) {
        List<Product> productListBuy = productRepository.findAllByBusinessIdAndActiveTrueAndBuyDollarTrueOrSaleDollarTrue(businessId);
        editPriceHelper(productListBuy, course);
    }

    private void editPriceHelper(List<Product> productList, double course) {
        for (Product product : productList) {
            if (product.getType().equals(Type.MANY)){
                editSalePriceManyHelper(productTypePriceRepository.findAllByProductId(product.getId()), course);
            }else {
                product.setSalePrice(course * product.getSalePriceDollar());
            }
        }
        productRepository.saveAll(productList);
    }

    private void editSalePriceManyHelper(List<ProductTypePrice> productTypePriceList, double course) {
        for (ProductTypePrice productTypePrice : productTypePriceList) {
            productTypePrice.setSalePrice(course * productTypePrice.getSalePriceDollar());
        }
        productTypePriceRepository.saveAll(productTypePriceList);
    }
}

