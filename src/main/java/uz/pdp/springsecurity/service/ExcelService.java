package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.entity.Currency;
import uz.pdp.springsecurity.enums.Type;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ExcelDto;
import uz.pdp.springsecurity.payload.ExportExcelDto;
import uz.pdp.springsecurity.payload.ProductViewDtos;
import uz.pdp.springsecurity.repository.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelService {
    private  final ProductRepository productRepository;

    private  final WarehouseRepository warehouseRepository;

    private  final MeasurementRepository measurementRepository;

    private  final BranchRepository branchRepository;

    private  final CategoryRepository categoryRepository;

    private  final ProductTypePriceRepository productTypePriceRepository;

    private  final CurrencyRepository currencyRepository;

    private final ProductTypeValueRepository productTypeValueRepository;

    private final ProductService productService;



    @Autowired
    BrandRepository brandRepository;
    private final FifoCalculationRepository fifoCalculationRepository;

    public List<ProductViewDtos> getByBusiness(UUID businessId) {

        boolean checkingBranch = false;
        Optional<Branch> optionalBranch = branchRepository.findById(businessId);
        if (optionalBranch.isPresent()){
            checkingBranch = true;
        }

        List<ProductViewDtos> productViewDtoList = new ArrayList<>();
        List<Product> productList = null;
        productList = productRepository.findAllByBranchIdAndActiveTrue(businessId);
        if (productList.isEmpty()){
            productList = productRepository.findAllByBusiness_IdAndActiveTrue(businessId);
        }
        if (productList.isEmpty()) {
            return null;
        } else {
            for (Product product : productList) {
                ProductViewDtos productViewDto = new ProductViewDtos();
                productViewDto.setProductName(product.getName());
                if (product.getBrand() != null)productViewDto.setBrandName(product.getBrand().getName());
                productViewDto.setBarcode(productViewDto.getBarcode());
                productViewDto.setBuyPrice(product.getBuyPrice());
                productViewDto.setSalePrice(product.getSalePrice());
                productViewDto.setMinQuantity(product.getMinQuantity());
                productViewDto.setExpiredDate(product.getExpireDate());
                Optional<Measurement> optionalMeasurement = measurementRepository.findById(product.getMeasurement().getId());
                optionalMeasurement.ifPresent(measurement -> productViewDto.setMeasurementId(measurement.getName()));
                if (checkingBranch){
                    Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(businessId, product.getId());
                    optionalWarehouse.ifPresent(warehouse -> productViewDto.setAmount(warehouse.getAmount()));
                }else {
                    List<Warehouse> warehouseList = warehouseRepository.findAllByBranch_BusinessIdAndProductId(businessId, product.getId());
                    double amount = 0;
                    for (Warehouse warehouse : warehouseList) {
                            amount+=warehouse.getAmount();
                    }
                    productViewDto.setAmount(amount);
                }
                productViewDtoList.add(productViewDto);
            }
            return productViewDtoList;
        }
    }


    public ApiResponse save(MultipartFile file, UUID categoryId, UUID measurementId, UUID branchId,UUID brandId) {

        Business business = null;

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        Optional<Measurement> optionalMeasurement = measurementRepository.findById(measurementId);
        Brand brand = null;
        if (brandId != null){
            Optional<Brand> optionalBrand = brandRepository.findById(brandId);
            brand = optionalBrand.get();
        }
        Category category = null;
        if (categoryId != null){
            Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
            category = optionalCategory.get();
        }

        if (optionalBranch.isEmpty()){
            return new ApiResponse("NOT FOUND BRANCH", false);
        }

        if (optionalMeasurement.isEmpty()){
            return new ApiResponse("NOT FOUND MEASUREMENT", false);
        }

        try {
            business = optionalBranch.get().getBusiness();
            List<Branch> branchList = new ArrayList<>();
            branchList.add(optionalBranch.get());

            List<ExportExcelDto> exportExcelDtoList = ExcelHelper.excelToTutorials(file.getInputStream());
            List<Product> productList=new ArrayList<>();
            List<FifoCalculation> fifoCalculationList = new ArrayList<>();
            List<Warehouse> warehouseList = new ArrayList<>();
            int count = 0;
            for (ExportExcelDto excelDto : exportExcelDtoList) {

                if (checkProduct(branchId, optionalBranch, fifoCalculationList, excelDto.getBarcode(), excelDto.getAmount(), excelDto.getBuyPrice()))
                    continue;

                if (Objects.equals(excelDto.getProductName(), "")){
                    continue;
                }
                Product product=new Product();
                product.setBusiness(business);
                product.setName(excelDto.getProductName());
                product.setExpireDate(excelDto.getExpiredDate());
                boolean exists = productRepository.existsByBarcodeAndBusinessIdAndActiveTrue(excelDto.getBarcode(), optionalBranch.get().getBusiness().getId());
                boolean exists1 = productTypePriceRepository.existsByBarcodeAndProduct_BusinessId(excelDto.getBarcode(), optionalBranch.get().getBusiness().getId());
                if (exists && exists1) {
                    continue;
                }
                product.setBarcode(String.valueOf(excelDto.getBarcode()));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                if (excelDto.getExpiredDate() != null){
                    product.setDueDate(formatter.parse(formatter.format(excelDto.getExpiredDate())));
                }else {
                    Date date=new Date();
                    product.setDueDate(date);
                }
                product.setBuyPrice(excelDto.getBuyPrice());
                product.setSalePrice(excelDto.getSalePrice());
                product.setMinQuantity(excelDto.getMinQuantity());
                product.setBranch(branchList);
                product.setTax(0);
                if (category!=null){
                    product.setCategory(category);
                }
                if (brand != null){
                    product.setBrand(brand);
                }
                product.setMeasurement(optionalMeasurement.get());
                product.setType(Type.SINGLE);
                product.setPhoto(null);
                Warehouse warehouse=new Warehouse();
                warehouse.setBranch(optionalBranch.get());
                warehouse.setAmount(excelDto.getAmount());
                warehouse.setProduct(product);
                fifoCalculationList.add(
                    new FifoCalculation(
                        optionalBranch.get(),
                        excelDto.getAmount(),
                        excelDto.getAmount(),
                        excelDto.getBuyPrice(),
                        new Date(),
                        product
                    )
                );
                warehouseList.add(warehouse);
                productList.add(product);
                count++;
            }
            if (exportExcelDtoList.size()>0){
                productRepository.saveAll(productList);
                warehouseRepository.saveAll(warehouseList);
                fifoCalculationRepository.saveAll(fifoCalculationList);
                return new ApiResponse("Successfully Added "+count+" Product",true);
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data:" + e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return new ApiResponse();
    }

    public ApiResponse saveExcel(MultipartFile file, UUID branchId) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);

        if (optionalBranch.isEmpty()) {
            return new ApiResponse("NOT FOUND BRANCH", false);
        }

        Business business = optionalBranch.get().getBusiness();
        List<Branch> branchList = Collections.singletonList(optionalBranch.get());
        Optional<Currency> optionalCurrency = currencyRepository.findByBusinessId(business.getId());
        Currency currency = null;
        if (optionalCurrency.isPresent()){
             currency = optionalCurrency.get();
        }

        try {
            List<ExcelDto> exportExcelDtoList = ExcelHelper.excelToTutorial(file.getInputStream());
            List<Product> productList = new ArrayList<>();
            List<FifoCalculation> fifoCalculationList = new ArrayList<>();
            List<Warehouse> warehouseList = new ArrayList<>();
            int count = 0;

            for (ExcelDto excelDto : exportExcelDtoList) {
                if (checkProduct(branchId, optionalBranch, fifoCalculationList, excelDto.getBarcode(), excelDto.getAmount(), excelDto.getBuyPrice())) {
                    continue;
                }

                if (excelDto.getName().isEmpty()) {
                    continue;
                }

                Measurement measurement = excelDto.getMeasurement() != null
                        ? measurementRepository.findByBusinessIdAndName(business.getId(), excelDto.getMeasurement()).orElse(null)
                        : null;
                Brand brand = excelDto.getBrand() != null
                        ? brandRepository.findAllByBusiness_IdAndName(business.getId(), excelDto.getBrand()).orElse(null)
                        : null;
                Category category = categoryRepository.findAllByBusiness_IdAndName(business.getId(), excelDto.getCategory()).orElse(null);

                Product product = new Product();
                product.setBusiness(business);
                product.setName(excelDto.getName());
                product.setExpireDate(excelDto.getExpiredDate());
                product.setBarcode(generateBarcode(business.getId(),product.getName(),product.getId(),false));

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dueDate = (excelDto.getExpiredDate() != null)
                        ? formatter.parse(formatter.format(excelDto.getExpiredDate()))
                        : new Date();
                product.setDueDate(dueDate);

                product.setMeasurement(measurement);
                product.setBrand(brand);
                product.setCategory(category);
                product.setBuyPrice(excelDto.getBuyPrice());
                product.setSalePrice(excelDto.getSalePrice());
                product.setMinQuantity(excelDto.getAlertQuantity());
                product.setBranch(branchList);
                product.setTax(0);
                product.setActive(true);
                product.setPhoto(null);

                Warehouse warehouse = new Warehouse();
                warehouse.setBranch(optionalBranch.get());
                warehouse.setAmount(excelDto.getAmount());
                warehouse.setProduct(product);

                fifoCalculationList.add(new FifoCalculation(
                        optionalBranch.get(),
                        excelDto.getAmount(),
                        excelDto.getAmount(),
                        excelDto.getBuyPrice(),
                        new Date(),
                        product
                ));
                if (excelDto.getTypeSize() != null || excelDto.getTypeColor() != null){
                    product.setType(Type.MANY);
                    productRepository.save(product);
                    String typeSizes = excelDto.getTypeColor();
                    String typeColor = excelDto.getTypeSize();

                    double parseDouble = Double.parseDouble(typeSizes);
                    int typeSize = (int) parseDouble;

                    ProductTypeValue productTypeValueColor = productTypeValueRepository
                            .findAllByProductType_BusinessIdAndName(business.getId(), String.valueOf(typeSize))
                            .orElse(null);

                    ProductTypeValue productTypeValueSize = productTypeValueRepository
                            .findAllByProductType_BusinessIdAndName(business.getId(), typeColor)
                            .orElse(null);

                    assert productTypeValueColor != null;
                    ProductType productType = productTypeValueColor.getProductType();


                    ProductTypePrice productTypePrice = new ProductTypePrice();
                    productTypePrice.setProduct(product);
                    if (productTypeValueSize!=null){
                        productTypePrice.setName(product.getName() + " ( " +productTypeValueColor.getName() + " " + productTypeValueSize.getName() + " )");
                        productTypePrice.setSubProductTypeValue(productTypeValueSize);
                    }else {
                        productTypePrice.setName(product.getName() + "( " + productTypeValueColor.getProductType().getName() + " - " + productTypeValueColor.getName() + " )");
                    }

                    productTypePrice.setProductTypeValue(productTypeValueColor);
                    productTypePrice.setBuyPrice(excelDto.getBuyPrice());
                    productTypePrice.setBuyPriceDollar(Math.round(excelDto.getBuyPrice() / currency.getCourse() * 100) / 100.);
                    productTypePrice.setSalePrice(excelDto.getSalePrice());
                    productTypePrice.setSalePriceDollar(Math.round(excelDto.getSalePrice() / currency.getCourse() * 100) / 100.);
                    productTypePrice.setGrossPrice(excelDto.getSalePrice());
                    productTypePrice.setGrossPriceDollar(Math.round(excelDto.getWholeSale() / currency.getCourse() * 100) / 100.);
                    productTypePrice.setProfitPercent(10);
                    productTypePrice.setPhoto(null);
                    productTypePrice.setBarcode(excelDto.getBarcode());
                    productTypePriceRepository.save(productTypePrice);

                    Warehouse warehouse1 = new Warehouse();
                    warehouse1.setBranch(optionalBranch.get());
                    warehouse1.setProductTypePrice(productTypePrice);
                    warehouse1.setAmount(excelDto.getAmount());
                    warehouseRepository.save(warehouse1);

                }else {
                    product.setType(Type.SINGLE);
                    warehouseList.add(warehouse);
                    productList.add(product);
                }


            }

            if (!productList.isEmpty()) {
                productRepository.saveAll(productList);
                warehouseRepository.saveAll(warehouseList);
                fifoCalculationRepository.saveAll(fifoCalculationList);
                return new ApiResponse("Successfully Added " + count + " Product", true);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store excel data: " + e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return new ApiResponse();
    }

    private  String generateBarcode(UUID businessId, String productName, UUID productId, boolean isUpdate) {
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

    private ApiResponse addProductMany(Product product, ExcelDto excelDto) {
        product.setType(Type.MANY);



        return null;
    }

    private ApiResponse addProductSingle(Product product, ExcelDto excelDto) {
        product.setType(Type.SINGLE);




        return null;
    }


    private boolean checkProduct(UUID branchId, Optional<Branch> optionalBranch, List<FifoCalculation> fifoCalculationList, String barcode, double amount, double buyPrice) {
        Optional<Product> optionalProduct = productRepository.findByBarcodeAndBranch_IdAndActiveTrue(barcode, branchId);
        if (optionalProduct.isPresent()){
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branchId,optionalProduct.get().getId());
            if (optionalWarehouse.isPresent()){
                Warehouse warehouse = optionalWarehouse.get();
                warehouse.setAmount(amount + warehouse.getAmount());
                fifoCalculationList.add(
                        new FifoCalculation(
                                optionalBranch.get(),
                                amount,
                                amount,
                                buyPrice,
                                new Date(),
                                optionalProduct.get()
                        )
                );
            }
            return true;
        }
        return false;
    }
}