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

        Production production = new Production();
        if (productionDto.getProductId() != null) {
            Optional<Product> optional = productRepository.findById(productionDto.getProductId());
            if (optional.isEmpty())return new ApiResponse("NOT FOUND PRODUCT", false);
            production.setProduct(optional.get());
        } else {
            Optional<ProductTypePrice> optional = productTypePriceRepository.findById(productionDto.getProductTypePriceId());
            if (optional.isEmpty())return new ApiResponse("NOT FOUND PRODUCT TYPE PRICE", false);
            production.setProductTypePrice(optional.get());
        }
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
                contentProduct = fifoCalculationService.createContentProduct(branch, contentProduct);
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

        double taskPrice = task.getTaskPrice();
//        if (task.isEach())taskPrice *= task.getUsers().size();

        Production production = new Production();
        production.setBranch(branch);
        production.setTotalQuantity(productionTaskDto.getTotalQuantity());
        production.setQuantity(productionTaskDto.getTotalQuantity() - productionTaskDto.getInvalid());
        production.setInvalid(productionTaskDto.getInvalid());
        production.setDate(productionTaskDto.getDate());
        production.setCostEachOne(false);
        production.setCost(taskPrice);
        production.setContentPrice(productionTaskDto.getContentPrice());
        production.setTotalPrice(productionTaskDto.getContentPrice());
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
//        salaryCountService.addForTask(task);
//        prizeService.addForTask(task);
        return new ApiResponse("SUCCESS", true);
    }
}
