package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.mapper.ExchangeProductMapper;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    @Autowired
    WarehouseRepository warehouseRepository;
    @Autowired
    PurchaseProductRepository purchaseProductRepository;

    private final ProductRepository productRepository;

    private final ProductTypePriceRepository productTypePriceRepository;

    private final ExchangeProductMapper exchangeProductMapper;

    private final ExchangeProductRepository exchangeProductRepository;
    @Autowired
    private ProductTypeComboRepository productTypeComboRepository;
    @Autowired
    private ExchangeProductBranchRepository exchangeProductBranchRepository;
    private final FifoCalculationService fifoCalculationService;

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

    private void createOrEditWareHouseHelper(Branch branch, Product product, ProductTypePrice productTypePrice, Double quantity) {
        Warehouse warehouse = null;
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

    /**
     * RETURN TRADEPRODUCT BY TRADEPRODUCTDTO AFTER CHECK AMOUNT
     *
     * @param branch
     * @param tradeProductDto
     * @return
     */
    public TradeProduct createOrEditTrade(Branch branch, TradeProduct tradeProduct, TradeProductDto tradeProductDto) {
        double amount = tradeProduct.getTradedQuantity() - tradeProductDto.getTradedQuantity();
        if (tradeProductDto.getType().equalsIgnoreCase("single")) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch.getId(), tradeProductDto.getProductId());
            if (optionalWarehouse.isEmpty()) return null;
            Warehouse warehouse = optionalWarehouse.get();
            warehouse.setAmount(warehouse.getAmount() + amount);
            warehouseRepository.save(warehouse);
            tradeProduct.setProduct(warehouse.getProduct());
        } else if (tradeProductDto.getType().equalsIgnoreCase("many")) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branch.getId(), tradeProductDto.getProductTypePriceId());
            if (optionalWarehouse.isEmpty()) return null;
            Warehouse warehouse = optionalWarehouse.get();
            warehouse.setAmount(warehouse.getAmount() + amount);
            warehouseRepository.save(warehouse);
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
                warehouseRepository.save(warehouse);
            }
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
            warehouseRepository.save(warehouse);
            contentProduct.setProduct(warehouse.getProduct());
        } else {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(contentProduct.getProduction().getBranch().getId(), contentProductDto.getProductTypePriceId());
            if (optionalWarehouse.isEmpty()) return null;
            Warehouse warehouse = optionalWarehouse.get();
            warehouse.setAmount(warehouse.getAmount() - contentProductDto.getQuantity());
            warehouseRepository.save(warehouse);
            contentProduct.setProductTypePrice(warehouse.getProductTypePrice());
        }
        return contentProduct;
    }

    public ApiResponse createOrUpdateExchangeProductBranch(ExchangeProductBranchDTO branchDTO, ExchangeProductBranch exchangeProductBranch, boolean update) {

        List<ExchangeProduct> exchangeProductList = new ArrayList<>();

        /**
         * create exchange product object list
         */
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
                        warehouseRepository.save(warehouse);
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
                        warehouseRepository.save(warehouse);
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
                        if (branch.getId().equals(receivedBranch)) {
                            b = true;
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

        if (branchId != null) {
            allWarehouse = warehouseRepository
                    .findAllByBranchIdAndAmountNotOrderByAmountAsc(branchId, 0, pageable);
        } else {
            allWarehouse = warehouseRepository
                    .findAllByBranch_BusinessIdAndAmountNotOrderByAmountAsc(businessId, 0, pageable);
        }

        List<Warehouse> warehouseList = allWarehouse.toList();
        for (Warehouse warehouse : warehouseList) {
            GetLessProductDto getLessProductDto = new GetLessProductDto();
            if (warehouse.getProductTypePrice() != null) {
                getLessProductDto.setName(warehouse.getProductTypePrice().getName());
            } else {
                getLessProductDto.setName(warehouse.getProduct().getName());
            }
            getLessProductDto.setAmount(warehouse.getAmount());
            getLessProductDtoList.add(getLessProductDto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("getLessProduct", getLessProductDtoList);
        response.put("currentPage", allWarehouse.getNumber());
        response.put("totalItems", allWarehouse.getTotalElements());
        response.put("totalPages", allWarehouse.getTotalPages());
        return new ApiResponse("all", true, response);
    }
}
