package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.Type;
import uz.pdp.springsecurity.payload.ApiResponse;
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
    @Autowired
    ProductRepository productRepository;
    @Autowired
    WarehouseRepository warehouseRepository;
    @Autowired
    MeasurementRepository measurementRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductTypePriceRepository productTypePriceRepository;


    @Autowired
    BrandRepository brandRepository;
    private final FifoCalculationRepository fifoCalculationRepository;

    public List<ProductViewDtos> getByBusiness(UUID businessId) {
        List<ProductViewDtos> productViewDtoList = new ArrayList<>();
        List<Product> productList = productRepository.findAllByBranchIdAndActiveTrue(businessId);
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
                Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(businessId, product.getId());
                optionalWarehouse.ifPresent(warehouse -> productViewDto.setAmount(warehouse.getAmount()));
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
            return new ApiResponse("NOT FOUND BRANCH");
        }
        UUID businessId = optionalBranch.get().getBusiness().getId();


        if (optionalMeasurement.isEmpty()){
            return new ApiResponse("NOT FOUND MEASUREMENT");
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

                Optional<Product> optionalProduct = productRepository.findByBarcodeAndBranch_IdAndActiveTrue(excelDto.getBarcode(), branchId);
                if (optionalProduct.isPresent()){
                    Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branchId,optionalProduct.get().getId());
                    if (optionalWarehouse.isPresent()){
                        Warehouse warehouse = optionalWarehouse.get();
                        warehouse.setAmount(excelDto.getAmount()+ warehouse.getAmount());
                        fifoCalculationList.add(
                            new FifoCalculation(
                                optionalBranch.get(),
                                excelDto.getAmount(),
                                excelDto.getAmount(),
                                excelDto.getBuyPrice(),
                                new Date(),
                                optionalProduct.get()
                            )
                        );
                    }
                    continue;
                }

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
}