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

    public ApiResponse add(ProductionDto productionDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(productionDto.getBranchId());
        if (optionalBranch.isEmpty()) return new ApiResponse("NOT FOUND BRANCH", false);
        Branch branch = optionalBranch.get();

        if (productionDto.getInvalid() >= productionDto.getTotalQuantity())return new ApiResponse("WRONG QUANTITY", false);
        if (productionDto.getDate() == null)return new ApiResponse("NOT FOUND DATE", false);

        List<ContentProductDto> contentProductDtoList = productionDto.getContentProductDtoList();
        if (contentProductDtoList.isEmpty()) return new ApiResponse("NOT FOUND PRODUCT_LIST", false);
        if (!branch.getBusiness().getSaleMinus()) {
            HashMap<UUID, Double> map = new HashMap<>();
            for (ContentProductDto dto : contentProductDtoList) {
                if (dto.getProductId() != null) {
                    UUID productId = dto.getProductId();
                    if (!productRepository.existsById(productId)) return new ApiResponse("PRODUCT NOT FOUND", false);
                    map.put(productId, map.getOrDefault(productId, 0d) + dto.getQuantity());
                } else if (dto.getProductTypePriceId() != null) {
                    UUID productId = dto.getProductTypePriceId();
                    if (!productTypePriceRepository.existsById(productId))
                        return new ApiResponse("PRODUCT NOT FOUND", false);
                    map.put(productId, map.getOrDefault(productId, 0d) + dto.getQuantity());
                } else {
                    return new ApiResponse("PRODUCT NOT FOUND", false);
                }
            }
            if (!warehouseService.checkBeforeTrade(branch, map)) return new ApiResponse("NOT ENOUGH PRODUCT", false);
        }

        Production production = new Production();
        production.setBranch(branch);
        production.setTotalQuantity(productionDto.getTotalQuantity());
        production.setQuantity(productionDto.getTotalQuantity() - productionDto.getInvalid());
        production.setInvalid(productionDto.getInvalid());
        production.setDate(productionDto.getDate());
        production.setCostEachOne(productionDto.isCostEachOne());
        production.setContentPrice(productionDto.getContentPrice());
        production.setCost(productionDto.getCost());
        production.setTotalPrice(productionDto.getTotalPrice());

        productionRepository.save(production);
        List<ContentProduct>contentProductList = new ArrayList<>();

        double contentPrice = 0d;
        for (ContentProductDto contentProductDto : contentProductDtoList) {
            ContentProduct contentProduct = new ContentProduct();
            contentProduct.setProduction(production);
            ContentProduct savedContentProduct = warehouseService.createContentProduct(contentProduct, contentProductDto);
            if (savedContentProduct == null) continue;
            savedContentProduct.setQuantity(contentProductDto.getQuantity());
            savedContentProduct.setTotalPrice(contentProductDto.getTotalPrice());
            ContentProduct contentProductFifo = fifoCalculationService.createContentProduct(branch, savedContentProduct);
            contentPrice += contentProductFifo.getTotalPrice();
            contentProductList.add(savedContentProduct);
        }
        if (contentProductList.isEmpty()) return new ApiResponse("NOT FOUND CONTENT PRODUCTS", false);
        contentProductRepository.saveAll(contentProductList);
        production.setContentPrice(contentPrice);
        double cost = production.isCostEachOne()?production.getTotalQuantity():1;
        production.setTotalPrice(cost * production.getCost() + contentPrice);

        if (productionDto.getProductId() != null) {
            Optional<Product> optional = productRepository.findById(productionDto.getProductId());
            if (optional.isEmpty())return new ApiResponse("NOT FOUND PRODUCT", false);
            Product product = optional.get();
            product.setBuyPrice(production.getTotalPrice() / production.getQuantity());
            production.setProduct(product);
            productRepository.save(product);
        } else {
            Optional<ProductTypePrice> optional = productTypePriceRepository.findById(productionDto.getProductTypePriceId());
            if (optional.isEmpty())return new ApiResponse("NOT FOUND PRODUCT TYPE PRICE", false);
            ProductTypePrice productTypePrice = optional.get();
            productTypePrice.setBuyPrice(production.getTotalPrice() / production.getQuantity());
            production.setProductTypePrice(productTypePrice);
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

    public ApiResponse addForTask(ProductionTaskDto productionTaskDto) {
        Optional<Task> optionalTask = taskRepository.findById(productionTaskDto.getTaskId());
        if (optionalTask.isEmpty()) return new ApiResponse("TASK NOT FOUND", false);
        Optional<TaskStatus> optionalTaskStatus = taskStatusRepository.findById(productionTaskDto.getTaskStatusId());
        if (optionalTaskStatus.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        Task task = optionalTask.get();
        TaskStatus taskStatus = optionalTaskStatus.get();
        if (task.getTaskStatus().getName().equalsIgnoreCase("Completed") || task.getProduction() != null){
            return new ApiResponse("You can not change this task !", false);
        }
        if (task.getDependTask() != null) {
            Task depentTask = task.getDependTask();
            if (depentTask.getTaskStatus().getOrginalName() != null && !depentTask.getTaskStatus().getOrginalName().equals("Completed")) {
                return new ApiResponse("You can not change this task, Complete " + depentTask.getName() + " task", false);
            }
        }
        Branch branch = task.getBranch();

        List<ContentProductDto> contentProductDtoList = productionTaskDto.getContentProductDtoList();
        if (contentProductDtoList.isEmpty()) return new ApiResponse("NOT FOUND PRODUCT_LIST", false);
        if (!branch.getBusiness().getSaleMinus()) {
            HashMap<UUID, Double> map = new HashMap<>();
            for (ContentProductDto dto : contentProductDtoList) {
                if (dto.getProductId() != null) {
                    UUID productId = dto.getProductId();
                    if (!productRepository.existsById(productId)) return new ApiResponse("PRODUCT NOT FOUND", false);
                    map.put(productId, map.getOrDefault(productId, 0d) + dto.getQuantity());
                } else if (dto.getProductTypePriceId() != null) {
                    UUID productId = dto.getProductTypePriceId();
                    if (!productTypePriceRepository.existsById(productId))
                        return new ApiResponse("PRODUCT NOT FOUND", false);
                    map.put(productId, map.getOrDefault(productId, 0d) + dto.getQuantity());
                } else {
                    return new ApiResponse("PRODUCT NOT FOUND", false);
                }
            }
            if (!warehouseService.checkBeforeTrade(branch, map)) return new ApiResponse("NOT ENOUGH PRODUCT", false);
        }

        Production production = new Production();
        production.setBranch(branch);
        production.setTotalQuantity(productionTaskDto.getTotalQuantity());
        production.setQuantity(productionTaskDto.getTotalQuantity() - productionTaskDto.getInvalid());
        production.setInvalid(productionTaskDto.getInvalid());
        production.setDate(productionTaskDto.getDate());
        production.setCostEachOne(false);
        production.setCost(productionTaskDto.getCost());
        production.setContentPrice(productionTaskDto.getContentPrice());
        production.setTotalPrice(productionTaskDto.getTotalPrice());

        productionRepository.save(production);
        List<ContentProduct>contentProductList = new ArrayList<>();

        double contentPrice = 0d;
        for (ContentProductDto contentProductDto : contentProductDtoList) {
            ContentProduct contentProduct = new ContentProduct();
            contentProduct.setProduction(production);
            ContentProduct savedContentProduct = warehouseService.createContentProduct(contentProduct, contentProductDto);
            if (savedContentProduct == null) continue;
            savedContentProduct.setQuantity(contentProductDto.getQuantity());
            savedContentProduct.setTotalPrice(contentProductDto.getTotalPrice());
            ContentProduct contentProductFifo = fifoCalculationService.createContentProduct(branch, savedContentProduct);
            contentPrice += contentProductFifo.getTotalPrice();
            contentProductList.add(savedContentProduct);
        }
        if (contentProductList.isEmpty()) return new ApiResponse("NOT FOUND CONTENT PRODUCTS", false);
        contentProductRepository.saveAll(contentProductList);
        production.setContentPrice(contentPrice);
        production.setTotalPrice(production.getCost() + contentPrice);

        Content content = task.getContent();
        if (content.getProduct() != null) {
            Product product = content.getProduct();
            product.setBuyPrice(production.getTotalPrice() / production.getQuantity());
            production.setProduct(product);
            productRepository.save(product);
        } else {
            ProductTypePrice productTypePrice = content.getProductTypePrice();
            productTypePrice.setBuyPrice(production.getTotalPrice() / production.getQuantity());
            production.setProductTypePrice(productTypePrice);
            productTypePriceRepository.save(productTypePrice);
        }
        productionRepository.save(production);
        fifoCalculationService.createProduction(production);
        warehouseService.createOrEditWareHouse(production);
        task.setTaskStatus(taskStatus);
        task.setProduction(production);
        taskRepository.save(task);
        return new ApiResponse("SUCCESS", true);
    }
}
