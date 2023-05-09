package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductionService {
    private final ProductionRepository productionRepository;
    private final ContentProductRepository contentProductRepository;
    private final ProductRepository productRepository;
    private final ProductTypePriceRepository productTypePriceRepository;
    private final BranchRepository branchRepository;
    private final WarehouseService warehouseService;
    private final FifoCalculationService fifoCalculationService;
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final SalaryCountService salaryCountService;
    private final PrizeService prizeService;

    public ApiResponse add(ProductionDto productionDto) {

        Optional<Branch> optionalBranch = branchRepository.findById(productionDto.getBranchId());
        if (optionalBranch.isEmpty()) return new ApiResponse("NOT FOUND BRANCH", false);
        Branch branch = optionalBranch.get();
        if (productionDto.getInvalid() >= productionDto.getTotalQuantity())return new ApiResponse("WRONG QUANTITY", false);
        if (productionDto.getDate() == null)return new ApiResponse("NOT FOUND DATE", false);

        ApiResponse apiResponse = checkBeforeProduction(branch, productionDto.getContentProductDtoList());
        if (!apiResponse.isSuccess()) return apiResponse;

        Production production = new Production(
                branch,
                productionDto.getDate(),
                productionDto.getTotalQuantity(),
                productionDto.getTotalQuantity() - productionDto.getInvalid(),
                productionDto.getInvalid(),
                productionDto.getTotalPrice(),
                productionDto.getContentPrice(),
                productionDto.getCost(),
                productionDto.isCostEachOne()
        );
        if (productionDto.getProductId() != null) {
            Optional<Product> optional = productRepository.findById(productionDto.getProductId());
            if (optional.isEmpty())return new ApiResponse("NOT FOUND PRODUCT", false);
            production.setProduct(optional.get());
        } else {
            Optional<ProductTypePrice> optional = productTypePriceRepository.findById(productionDto.getProductTypePriceId());
            if (optional.isEmpty())return new ApiResponse("NOT FOUND PRODUCT TYPE PRICE", false);
            production.setProductTypePrice(optional.get());
        }
        productionRepository.save(production);

        ApiResponse apiResponseSave = saveContentProductList(production, productionDto.getContentProductDtoList());
        if (!apiResponseSave.isSuccess()) return apiResponse;

        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse addForTask(ProductionTaskDto productionTaskDto) {
        Optional<Task> optionalTask = taskRepository.findById(productionTaskDto.getTaskId());
        if (optionalTask.isEmpty()) return new ApiResponse("TASK NOT FOUND", false);
        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(productionTaskDto.getTaskStatusId());
        if (optionalTaskStatus.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        Task task = optionalTask.get();
        TaskStatus taskStatus = optionalTaskStatus.get();
        if (task.getTaskStatus().getOrginalName() != null && task.getTaskStatus().getOrginalName().equals("Completed")){
            return new ApiResponse("You can not change this task !", false);
        }
        if (task.getDependTask() != null && taskStatus.getOrginalName().equals("Completed")) {
            Task depentTask = task.getDependTask();
            if (depentTask.getTaskStatus().getOrginalName() != null && !depentTask.getTaskStatus().getOrginalName().equals("Completed")) {
                return new ApiResponse("You can not change this task, Complete " + depentTask.getName() + " task", false);
            }
        }
        Branch branch = task.getBranch();

        ApiResponse apiResponse = checkBeforeProduction(branch, productionTaskDto.getContentProductDtoList());
        if (!apiResponse.isSuccess()) return apiResponse;

        Production production = new Production(
                branch,
                productionTaskDto.getDate(),
                productionTaskDto.getTotalQuantity(),
                productionTaskDto.getTotalQuantity() - productionTaskDto.getInvalid(),
                productionTaskDto.getInvalid(),
                productionTaskDto.getContentPrice() + task.getTaskPrice(),
                productionTaskDto.getContentPrice(),
                task.getTaskPrice(),
                false
        );
        if (task.getContent().getProduct() != null) {
            production.setProduct(task.getContent().getProduct());
        } else {
            production.setProductTypePrice(task.getContent().getProductTypePrice());
        }
        productionRepository.save(production);

        ApiResponse apiResponseSave = saveContentProductList(production, productionTaskDto.getContentProductDtoList());
        if (!apiResponseSave.isSuccess()) return apiResponse;

        task.setTaskStatus(taskStatus);
        task.setProduction(production);
        taskRepository.save(task);
        salaryCountService.addForTask(task);
        prizeService.addForTask(task);
        return new ApiResponse("SUCCESS", true);
    }

    private ApiResponse checkBeforeProduction(Branch branch, List<ContentProductDto> contentProductDtoList) {
        if (contentProductDtoList.isEmpty()) return new ApiResponse("NOT FOUND PRODUCT_LIST", false);
        if (!branch.getBusiness().getSaleMinus()) {
            HashMap<UUID, Double> map = new HashMap<>();
            for (ContentProductDto dto : contentProductDtoList) {
                if (dto.getProductId() != null) {
                    UUID productId = dto.getProductId();
                    if (!productRepository.existsById(productId)) return new ApiResponse("PRODUCT NOT FOUND", false);
                    if (!dto.isByProduct())
                        map.put(productId, map.getOrDefault(productId, 0d) + dto.getQuantity());
                } else if (dto.getProductTypePriceId() != null) {
                    UUID productId = dto.getProductTypePriceId();
                    if (!productTypePriceRepository.existsById(productId))
                        return new ApiResponse("PRODUCT NOT FOUND", false);
                    if (!dto.isByProduct())
                        map.put(productId, map.getOrDefault(productId, 0d) + dto.getQuantity());
                } else {
                    return new ApiResponse("PRODUCT NOT FOUND", false);
                }
            }
            if (!warehouseService.checkBeforeTrade(branch, map)) return new ApiResponse("NOT ENOUGH PRODUCT", false);
        }
        return new ApiResponse("SUCCESS", true);
    }

    private ApiResponse saveContentProductList(Production production, List<ContentProductDto> contentProductDtoList) {
        List<ContentProduct>contentProductList = new ArrayList<>();
        double contentPrice = 0d;
        for (ContentProductDto contentProductDto : contentProductDtoList) {
            if (contentProductDto.getQuantity() == 0)continue;
            ContentProduct contentProduct = new ContentProduct();
            contentProduct.setProduction(production);
            contentProduct.setQuantity(contentProductDto.getQuantity());
            contentProduct.setTotalPrice(contentProductDto.getTotalPrice());
            contentProduct.setByProduct(contentProductDto.isByProduct());
            if (contentProductDto.isByProduct()) {
                contentProduct = warehouseService.createByProduct(contentProduct, contentProductDto);
                if (contentProduct == null) continue;
                fifoCalculationService.createByProduct(production, contentProduct);
                contentPrice -= contentProduct.getTotalPrice();
            }else {
                contentProduct = warehouseService.createContentProduct(contentProduct, contentProductDto);
                if (contentProduct == null) continue;
                contentProduct = fifoCalculationService.createContentProduct(production.getBranch(), contentProduct);
                contentPrice += contentProduct.getTotalPrice();
            }
            contentProductList.add(contentProduct);
        }
        if (contentProductList.isEmpty()) return new ApiResponse("NOT FOUND CONTENT PRODUCTS", false);
        contentProductRepository.saveAll(contentProductList);
        production.setContentPrice(contentPrice);
        double cost = production.isCostEachOne()?production.getTotalQuantity():1;
        production.setTotalPrice(cost * production.getCost() + contentPrice);

        if (production.getProduct() != null) {
            Product product = production.getProduct();
            product.setBuyPrice(production.getTotalPrice() / production.getQuantity());
            productRepository.save(product);
        } else {
            ProductTypePrice productTypePrice = production.getProductTypePrice();
            productTypePrice.setBuyPrice(production.getTotalPrice() / production.getQuantity());
            productTypePriceRepository.save(productTypePrice);
        }
        productionRepository.save(production);
        fifoCalculationService.createProduction(production);
        warehouseService.createOrEditWareHouse(production);
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse getAll(UUID branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) return new ApiResponse("NOT FOUND BRANCH", false);
        List<Production> productionList = productionRepository.findAllByBranchId(branchId);
        if (productionList.isEmpty())return new ApiResponse("NOT FOUND", false);
        return new ApiResponse(true, productionList);
    }

    public ApiResponse getOne(UUID productionId) {
        Optional<Production> optionalProduction = productionRepository.findById(productionId);
        if (optionalProduction.isEmpty())return new ApiResponse("NOT FOUND", false);
        Production production = optionalProduction.get();
        List<ContentProduct> contentProductList = contentProductRepository.findAllByProductionId(productionId);
        if (contentProductList.isEmpty()) return new ApiResponse("NOT FOUND CONTENT PRODUCTS", false);
        GetOneContentProductionDto getOneContentProductionDto = new GetOneContentProductionDto(
                production,
                contentProductList
        );
        return new ApiResponse(true, getOneContentProductionDto);
    }
}
