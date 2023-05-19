package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final ProductTypePriceRepository productTypePriceRepository;
    private final ExchangeProductRepository exchangeProductRepository;
    private final ProductTypeComboRepository productTypeComboRepository;
    private final ExchangeProductBranchRepository exchangeProductBranchRepository;
    private final FifoCalculationService fifoCalculationService;
    private final NotificationService notificationService;


    public void createOrEditWareHouse(PurchaseProduct purchaseProduct, double quantity) {
        Branch branch = purchaseProduct.getPurchase().getBranch();
        Product product = purchaseProduct.getProduct();
        ProductTypePrice productTypePrice = purchaseProduct.getProductTypePrice();
        createOrEditWareHouseHelper(branch, product, productTypePrice, quantity);
    }


    public void createOrEditWareHouse(Production production) {
        Branch branch = production.getBranch();
        Product product = production.getProduct();
        ProductTypePrice productTypePrice = production.getProductTypePrice();
        createOrEditWareHouseHelper(branch, product, productTypePrice, production.getQuantity());
    }


    public void createOrEditWareHouseHelper(Branch branch, Product product, ProductTypePrice productTypePrice, Double quantity) {
        Warehouse warehouse;
        if (product != null) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch.getId(), product.getId());
            if (optionalWarehouse.isPresent()) {
                warehouse = optionalWarehouse.get();
                if (branch.getBusiness().getSaleMinus() && warehouse.getAmount() < 0) {
                    TradeProduct tradeProduct = new TradeProduct();
                    tradeProduct.setProduct(product);
                    double amount = -warehouse.getAmount();
                    fifoCalculationService.createOrEditTradeProduct(branch, tradeProduct, amount > quantity ? quantity : amount);
                }
                warehouse.setAmount(warehouse.getAmount() + quantity);
            } else {
                warehouse = new Warehouse();
                warehouse.setBranch(branch);
                warehouse.setProduct(product);
                warehouse.setAmount(quantity);
            }
        } else {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branch.getId(), productTypePrice.getId());
            if (optionalWarehouse.isPresent()) {
                warehouse = optionalWarehouse.get();
                if (branch.getBusiness().getSaleMinus() && warehouse.getAmount() < 0) {
                    TradeProduct tradeProduct = new TradeProduct();
                    tradeProduct.setProductTypePrice(productTypePrice);
                    double amount = -warehouse.getAmount();
                    fifoCalculationService.createOrEditTradeProduct(branch, tradeProduct, amount > quantity ? quantity : amount);
                }
                warehouse.setAmount(warehouse.getAmount() + quantity);
            } else {
                warehouse = new Warehouse();
                warehouse.setBranch(branch);
                warehouse.setProductTypePrice(productTypePrice);
                warehouse.setAmount(quantity);
            }
        }
        warehouseRepository.save(warehouse);
    }


    public Boolean checkBeforeTrade(Branch branch, HashMap<UUID, Double> map) {
        for (Map.Entry<UUID, Double> entry : map.entrySet()) {
            Warehouse warehouse = null;
            if (warehouseRepository.existsByBranchIdAndProductId(branch.getId(), entry.getKey())) {
                Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch.getId(), entry.getKey());
                if (optionalWarehouse.isPresent()) warehouse = optionalWarehouse.get();
            } else if (warehouseRepository.existsByBranchIdAndProductTypePriceId(branch.getId(), entry.getKey())) {
                Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branch.getId(), entry.getKey());
                if (optionalWarehouse.isPresent()) warehouse = optionalWarehouse.get();
            } else return false;
            if (warehouse == null) return false;
            if (warehouse.getAmount() < entry.getValue()) return false;
        }
        return true;
    }

    public TradeProduct createOrEditTrade(Branch branch, TradeProduct tradeProduct, TradeProductDto tradeProductDto) {
        double amount = tradeProduct.getTradedQuantity() - tradeProductDto.getTradedQuantity();
        if (tradeProductDto.getType().equalsIgnoreCase("single")) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch.getId(), tradeProductDto.getProductId());
            if (optionalWarehouse.isEmpty()) return null;
            Warehouse warehouse = optionalWarehouse.get();
            warehouse.setAmount(warehouse.getAmount() + amount);
            warehouse.setLastSoldDate(new Date());
            Warehouse save = warehouseRepository.save(warehouse);
            if (warehouse.getAmount() <= warehouse.getProduct().getMinQuantity()) {
                notificationService.lessProduct(warehouse.getProduct().getId(), true, save.getAmount());
            }
            tradeProduct.setProduct(warehouse.getProduct());
        } else if (tradeProductDto.getType().equalsIgnoreCase("many")) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branch.getId(), tradeProductDto.getProductTypePriceId());
            if (optionalWarehouse.isEmpty()) return null;
            Warehouse warehouse = optionalWarehouse.get();
            warehouse.setAmount(warehouse.getAmount() + amount);
            warehouse.setLastSoldDate(new Date());
            Warehouse save = warehouseRepository.save(warehouse);
            if (warehouse.getAmount() <= warehouse.getProductTypePrice().getProduct().getMinQuantity()) {
                notificationService.lessProduct(warehouse.getProductTypePrice().getId(), false, save.getAmount());
            }
            tradeProduct.setProductTypePrice(warehouse.getProductTypePrice());
        } else {
            Optional<Product> optionalProduct = productRepository.findById(tradeProductDto.getProductId());
            if (optionalProduct.isEmpty()) return null;
            List<ProductTypeCombo> comboList = productTypeComboRepository.findAllByMainProductId(tradeProductDto.getProductId());
            for (ProductTypeCombo combo : comboList) {
                Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch.getId(), combo.getContentProduct().getId());
                if (optionalWarehouse.isEmpty()) continue;
                Warehouse warehouse = optionalWarehouse.get();
                warehouse.setAmount(warehouse.getAmount() + amount * combo.getAmount());
                warehouse.setLastSoldDate(new Date());
                warehouseRepository.save(warehouse);
            } // TODO: 19.05.2023 add many type
            tradeProduct.setProduct(optionalProduct.get());
        }
        tradeProduct.setTotalSalePrice(tradeProductDto.getTotalSalePrice());
        tradeProduct.setTradedQuantity(tradeProductDto.getTradedQuantity());
        return tradeProduct;
    }


    public ContentProduct createContentProduct(ContentProduct contentProduct, ContentProductDto contentProductDto) {
        if (contentProductDto.getProductId() != null) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(contentProduct.getProduction().getBranch().getId(), contentProductDto.getProductId());
            if (optionalWarehouse.isEmpty()) return null;
            Warehouse warehouse = optionalWarehouse.get();
            warehouse.setAmount(warehouse.getAmount() - contentProductDto.getQuantity());
            Warehouse save = warehouseRepository.save(warehouse);
            if (warehouse.getAmount() <= warehouse.getProduct().getMinQuantity()) {
                notificationService.lessProduct(warehouse.getProduct().getId(),true,save.getAmount());
            }
            contentProduct.setProduct(warehouse.getProduct());
        } else {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(contentProduct.getProduction().getBranch().getId(), contentProductDto.getProductTypePriceId());
            if (optionalWarehouse.isEmpty()) return null;
            Warehouse warehouse = optionalWarehouse.get();
            warehouse.setAmount(warehouse.getAmount() - contentProductDto.getQuantity());
            Warehouse save = warehouseRepository.save(warehouse);
            if (warehouse.getAmount() <= warehouse.getProductTypePrice().getProduct().getMinQuantity()) {
                notificationService.lessProduct(warehouse.getProductTypePrice().getId(), false, save.getAmount());
            }
            contentProduct.setProductTypePrice(warehouse.getProductTypePrice());
        }
        return contentProduct;
    }

    public ContentProduct createByProduct(ContentProduct contentProduct, ContentProductDto contentProductDto) {
        if (contentProductDto.getProductId() != null) {
            Optional<Product> optional = productRepository.findById(contentProductDto.getProductId());
            if (optional.isEmpty()) return null;
            contentProduct.setProduct(optional.get());
        } else {
            Optional<ProductTypePrice> optional = productTypePriceRepository.findById(contentProductDto.getProductTypePriceId());
            if (optional.isEmpty()) return null;
            contentProduct.setProductTypePrice(optional.get());
        }
        createOrEditWareHouseHelper(contentProduct.getProduction().getBranch(), contentProduct.getProduct(), contentProduct.getProductTypePrice(), contentProductDto.getQuantity());
        return contentProduct;
    }

    public ApiResponse createOrUpdateExchangeProductBranch(ExchangeProductBranchDTO branchDTO, ExchangeProductBranch exchangeProductBranch, boolean update) {

        List<ExchangeProduct> exchangeProductList = new ArrayList<>();
        for (ExchangeProductDTO exchangeProductDTO : branchDTO.getExchangeProductDTOS()) {
            ExchangeProduct exchangeProduct = new ExchangeProduct();
            exchangeProduct.setExchangeProductQuantity(exchangeProductDTO.getExchangeProductQuantity());
            if (exchangeProductDTO.getProductExchangeId() != null) {
                Optional<Product> optionalProduct = productRepository.findById(exchangeProductDTO.getProductExchangeId());
                optionalProduct.ifPresent(exchangeProduct::setProduct);
            } else {
                Optional<ProductTypePrice> optionalProductTypePrice = productTypePriceRepository
                        .findById(exchangeProductDTO.getProductTypePriceId());
                optionalProductTypePrice.ifPresent(exchangeProduct::setProductTypePrice);
            }
            exchangeProductList.add(exchangeProduct);
            exchangeProductRepository.save(exchangeProduct);
        }

        Branch shippedBranch = exchangeProductBranch.getShippedBranch();
        Branch receivedBranch = exchangeProductBranch.getReceivedBranch();

        for (ExchangeProduct exchangeProduct : exchangeProductList) {
            if (exchangeProduct.getProduct() != null) {
                Optional<Warehouse> optionalShippedBranchWarehouse = warehouseRepository
                        .findByBranchIdAndProductId(shippedBranch.getId(), exchangeProduct.getProduct().getId());
                Optional<Warehouse> optionalReceivedBranchWarehouse = warehouseRepository
                        .findByBranchIdAndProductId(receivedBranch.getId(), exchangeProduct.getProduct().getId());
                if (optionalShippedBranchWarehouse.isPresent()) {
                    Warehouse warehouse = optionalShippedBranchWarehouse.get();
                    if (warehouse.getAmount() >= exchangeProduct.getExchangeProductQuantity()) {
                        warehouse.setAmount(warehouse.getAmount() - exchangeProduct.getExchangeProductQuantity());
                        Warehouse save = warehouseRepository.save(warehouse);
                        if (warehouse.getAmount() <= warehouse.getProduct().getMinQuantity()) {
                            notificationService.lessProduct(warehouse.getProduct().getId(), true, save.getAmount());
                        }

                    } else {
                        return new ApiResponse("Omborda mahsulot yetarli emas!");
                    }
                }
                if (optionalReceivedBranchWarehouse.isPresent()) {
                    Warehouse warehouse = optionalReceivedBranchWarehouse.get();
                    warehouse.setAmount(warehouse.getAmount() + exchangeProduct.getExchangeProductQuantity());
                    warehouseRepository.save(warehouse);
                } else {
                    List<Branch> branchList = exchangeProduct.getProduct().getBranch();
                    Warehouse warehouse = new Warehouse();
                    boolean b = false;
                    for (Branch branch : branchList) {
                        if (branch.getId().equals(receivedBranch.getId())) {
                            b = true;
                            break;
                        }
                    }
                    Optional<Product> optionalProduct = productRepository.findById(exchangeProduct.getProduct().getId());
                    if (!b) {
                        branchList.add(receivedBranch);
                        Product product = optionalProduct.get();
                        product.setBranch(branchList);
                        productRepository.save(product);
                    }
                    warehouse.setBranch(receivedBranch);
                    warehouse.setAmount(exchangeProduct.getExchangeProductQuantity());
                    optionalProduct.ifPresent(warehouse::setProduct);
                    warehouseRepository.save(warehouse);
                }
            } else {
                Optional<Warehouse> optionalShippedBranchWarehouse = warehouseRepository
                        .findByBranchIdAndProductTypePriceId(shippedBranch.getId(), exchangeProduct.getProductTypePrice().getId());
                Optional<Warehouse> optionalReceivedBranchWarehouse = warehouseRepository
                        .findByBranchIdAndProductTypePriceId(receivedBranch.getId(), exchangeProduct.getProductTypePrice().getId());
                if (optionalShippedBranchWarehouse.isPresent()) {
                    Warehouse warehouse = optionalShippedBranchWarehouse.get();
                    if (warehouse.getAmount() >= exchangeProduct.getExchangeProductQuantity()) {
                        warehouse.setAmount(warehouse.getAmount() - exchangeProduct.getExchangeProductQuantity());
                        Warehouse save = warehouseRepository.save(warehouse);
                        if (warehouse.getAmount() <= warehouse.getProductTypePrice().getProduct().getMinQuantity()) {
                            notificationService.lessProduct(warehouse.getProductTypePrice().getId(), false, save.getAmount());
                        }
                    } else {
                        return new ApiResponse("Omborda mahsulot yetarli emas!");
                    }
                }
                if (optionalReceivedBranchWarehouse.isPresent()) {
                    Warehouse warehouse = optionalReceivedBranchWarehouse.get();
                    warehouse.setAmount(warehouse.getAmount() + exchangeProduct.getExchangeProductQuantity());
                    warehouseRepository.save(warehouse);
                } else {
                    List<Branch> branchList = exchangeProduct.getProductTypePrice().getProduct().getBranch();
                    boolean b = false;

                    for (Branch branch : branchList) {
                        if (branch.getId().equals(receivedBranch.getId())) {
                            b = true;
                            break;
                        }
                    }

                    Optional<ProductTypePrice> optionalProductTypePrice = productTypePriceRepository.findById(exchangeProduct.getProductTypePrice().getId());
                    if (!b) {
                        branchList.add(receivedBranch);
                        Product product = exchangeProduct.getProductTypePrice().getProduct();
                        product.setBranch(branchList);
                        productRepository.save(product);
                    }

                    Warehouse warehouse = new Warehouse();
                    warehouse.setBranch(receivedBranch);
                    warehouse.setAmount(exchangeProduct.getExchangeProductQuantity());
                    optionalProductTypePrice.ifPresent(warehouse::setProductTypePrice);
                    warehouseRepository.save(warehouse);
                }
            }
        }
        List<ExchangeProduct> exchangeProducts = exchangeProductRepository.saveAll(exchangeProductList);
        exchangeProductBranch.setExchangeProductList(exchangeProducts);
        exchangeProductBranchRepository.save(exchangeProductBranch);
        fifoCalculationService.createExchange(exchangeProductBranch);
        return new ApiResponse("successfully saved", true);
    }


    public ApiResponse getLessProduct(UUID businessId, UUID branchId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Warehouse> allWarehouse;

        List<GetLessProductDto> getLessProductDtoList = new ArrayList<>();
        List<Warehouse> warehouses = new ArrayList<>();

        if (branchId != null) {
            allWarehouse = warehouseRepository
                    .findAllByBranchIdAndAmountNotOrderByAmountAsc(branchId, 0, pageable);
        } else {
            allWarehouse = warehouseRepository
                    .findAllByBranch_BusinessIdAndAmountNotOrderByAmountAsc(businessId, 0, pageable);
        }

        List<Warehouse> warehouseList = allWarehouse.toList();

        for (Warehouse warehouse : warehouseList) {
            if (warehouse.getProduct() != null) {
                if (warehouse.getProduct().getMinQuantity() >= warehouse.getAmount()) {
                    warehouses.add(warehouse);
                }
            } else {
                if (warehouse.getProductTypePrice().getProduct().getMinQuantity() >= warehouse.getAmount()) {
                    warehouses.add(warehouse);
                }
            }
        }

        for (Warehouse warehouse : warehouses) {
            GetLessProductDto getLessProductDto = new GetLessProductDto();
            if (warehouse.getProductTypePrice() != null) {
                getLessProductDto.setName(warehouse.getProductTypePrice().getName());
                if (warehouse.getProductTypePrice().getProduct().getPhoto() != null) {
                    getLessProductDto.setAttachmentId(warehouse.getProductTypePrice().getProduct().getPhoto().getId());
                }
            } else {
                getLessProductDto.setName(warehouse.getProduct().getName());
                if (warehouse.getProduct().getPhoto() != null) {
                    getLessProductDto.setAttachmentId(warehouse.getProduct().getPhoto().getId());
                }
            }
            getLessProductDto.setAmount(warehouse.getAmount());
            getLessProductDtoList.add(getLessProductDto);
        }

        Page<Warehouse> newPage = new PageImpl<>(warehouses);

        Map<String, Object> response = new HashMap<>();
        response.put("getLessProduct", getLessProductDtoList);
        response.put("currentPage", newPage.getNumber());
        response.put("totalItems", newPage.getTotalElements());
        response.put("totalPages", newPage.getTotalPages());
        return new ApiResponse("all", true, response);
    }

    public ApiResponse getLessSoldProduct(UUID branchId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Warehouse> warehousePage = warehouseRepository.findAllByBranchIdAndAmountIsNotOrderByLastSoldDate(branchId, 0, pageable);
        List<GetLessProductDto> getLessProductDtoList = new ArrayList<>();
        for (Warehouse warehouse : warehousePage.getContent()) {
            GetLessProductDto dto = new GetLessProductDto();
            if (warehouse.getProduct() != null)
                dto.setName(warehouse.getProduct().getName());
            else
                dto.setName(warehouse.getProductTypePrice().getName());
            dto.setAmount(warehouse.getAmount());
            dto.setLastSoldDate(warehouse.getLastSoldDate());
            getLessProductDtoList.add(dto);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("getLessProduct", getLessProductDtoList);
        response.put("currentPage", warehousePage.getNumber());
        response.put("totalItem", warehousePage.getTotalElements());
        response.put("totalPage", warehousePage.getTotalPages());
        return new ApiResponse("all", true, response);
    }
}
