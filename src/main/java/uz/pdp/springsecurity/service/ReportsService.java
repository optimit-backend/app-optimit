package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportsService {

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    ProductionRepository productionRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    BranchRepository branchRepository;
    @Autowired
    TradeProductRepository tradeProductRepository;
    @Autowired
    PurchaseRepository purchaseRepository;
    @Autowired
    PurchaseProductRepository purchaseProductRepository;
    @Autowired
    OutlayRepository outlayRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    TradeRepository tradeRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    OutlayCategoryRepository outlayCategoryRepository;

    @Autowired
    ProductTypePriceRepository productTypePriceRepository;

    private final static Date date = new Date();
    private final static Timestamp currentDay = new Timestamp(System.currentTimeMillis());
    private final static Timestamp enDate = new Timestamp(date.getTime());
    private final static LocalDateTime dateTime = enDate.toLocalDateTime();
    private final static LocalDateTime LAST_MONTH = dateTime.minusMonths(1);
    private final static LocalDate localDate = LocalDate.now();
    private final static LocalDateTime THIS_MONTH = localDate.withDayOfMonth(1).atStartOfDay();
    private final static LocalDate WEEK_START_DAY = localDate.minusDays(7 + localDate.getDayOfWeek().getValue() - 1);
    private final static LocalDate WEEK_END_DAY = localDate.minusDays(7 + localDate.getDayOfWeek().getValue() - 7);
    private final static LocalDate TEMP_START_OF_YEAR = LocalDate.of(localDate.getYear() - 1, 1, 1);
    private final static LocalDate TEMP_FOR_THIS_START_OF_YEAR = LocalDate.of(localDate.getYear(), 1, 1);
    private final static LocalDate TEMP_START_OF_DAY = localDate.minusDays(1);
    private final static LocalDate TEMP_END_OF_DAY = LocalDate.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth());
    private final static LocalDate TEMP_END_OF_YEAR = LocalDate.of(localDate.getYear() - 1, 12, 31);
    private final static LocalDate TEMP_START_OF_MONTH_ONE = LocalDate.of(localDate.getYear(), localDate.getMonth().getValue(), 1);
    private final static LocalDate TEMP_START_OF_MONTH = TEMP_START_OF_MONTH_ONE.minusMonths(1);
    private final static LocalDate TEMP_END_OF_MONTH = LocalDate.of(localDate.getYear(), TEMP_START_OF_MONTH.getMonth(), TEMP_START_OF_MONTH.lengthOfMonth());
    private final static LocalDateTime START_OF_YEAR = TEMP_START_OF_YEAR.atStartOfDay();
    private final static LocalDateTime START_OF_YEAR_FOR_THIS = TEMP_FOR_THIS_START_OF_YEAR.atStartOfDay();
    private final static LocalDateTime END_OF_YEAR = TEMP_END_OF_YEAR.atStartOfDay();
    private final static LocalDateTime START_OF_MONTH = TEMP_START_OF_MONTH.atStartOfDay();
    private final static LocalDateTime END_OF_MONTH = TEMP_END_OF_MONTH.atStartOfDay();
    private final static LocalDateTime START_OF_DAY = TEMP_START_OF_DAY.atStartOfDay();
    private final static LocalDateTime END_OF_DAY = TEMP_END_OF_DAY.atStartOfDay();

    public ApiResponse allProductAmount(UUID branchId, UUID brandId, UUID categoryId, String production) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);

        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }

        Optional<Business> optionalBusiness = businessRepository.findById(optionalBranch.get().getBusiness().getId());

        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("Business Not Found", false);
        }
        UUID businessId = optionalBranch.get().getBusiness().getId();
        List<Product> productList = new ArrayList<>();
        List<ProductTypePrice> productTypePriceList = new ArrayList<>();
        if (categoryId == null && production == null && brandId == null) {
            productList = productRepository.findAllByBranchIdAndActiveIsTrue(branchId);
            productTypePriceList = productTypePriceRepository.findAllByProduct_BranchIdAndProduct_ActiveIsTrue(branchId);
            if (productList.isEmpty()) {
                return new ApiResponse("No Found Products");
            }
        } else if (categoryId != null && brandId == null) {
            productList = productRepository.findAllByCategoryIdAndBranchIdAndActiveTrue(categoryId, branchId);
            productTypePriceList = productTypePriceRepository.findAllByProduct_Category_IdAndProduct_Branch_IdAndProduct_ActiveIsTrue(categoryId, branchId);
        } else if (categoryId == null && production == null) {
            productList = productRepository.findAllByBrandIdAndBranchIdAndActiveTrue(brandId, branchId);
            productTypePriceList = productTypePriceRepository.findAllByProduct_Brand_IdAndProduct_Branch_IdAndProduct_ActiveIsTrue(brandId, branchId);
        } else if (production != null && categoryId != null) {
            List<Production> productionList = productionRepository.findAllByProduct_CategoryIdAndProduct_BrandIdAndProduct_BranchId(categoryId, brandId, branchId);
            if (productionList.isEmpty()) {
                return new ApiResponse("Production Not Found", false);
            }

            List<Product> products = new ArrayList<>();
            List<ProductTypePrice> priceList = new ArrayList<>();
            for (Production productions : productionList) {
                if (productions.getProduct() != null) {
                    Optional<Product> optionalProduct = productRepository.findById(productions.getProduct().getId());
                    products.add(optionalProduct.get());
                } else {
                    Optional<ProductTypePrice> optionalProductTypePrice = productTypePriceRepository.findById(productions.getProductTypePrice().getId());
                    priceList.add(optionalProductTypePrice.get());
                }
            }
            productList = products;
            productTypePriceList = priceList;

        } else if (production != null && categoryId != null && brandId == null) {
            List<Production> productionList = productionRepository.findAllByProduct_CategoryIdAndProduct_BranchId(categoryId, branchId);
            if (productionList.isEmpty()) {
                return new ApiResponse("Production Not Found", false);
            }

            List<Product> products = new ArrayList<>();
            List<ProductTypePrice> priceList = new ArrayList<>();
            for (Production productions : productionList) {
                if (productions.getProduct() != null) {
                    Optional<Product> optionalProduct = productRepository.findById(productions.getProduct().getId());
                    products.add(optionalProduct.get());
                } else {
                    Optional<ProductTypePrice> optionalProductTypePrice = productTypePriceRepository.findById(productions.getProductTypePrice().getId());
                    priceList.add(optionalProductTypePrice.get());
                }
            }
            productList = products;
            productTypePriceList = priceList;

        } else if (production != null && categoryId == null && brandId != null) {
            List<Production> productionList = productionRepository.findAllByProduct_BrandIdAndProduct_BranchId(categoryId, branchId);
            if (productionList.isEmpty()) {
                return new ApiResponse("Production Not Found", false);
            }
            List<Product> products = new ArrayList<>();
            List<ProductTypePrice> priceList = new ArrayList<>();
            for (Production productions : productionList) {
                if (productions.getProduct() != null) {
                    Optional<Product> optionalProduct = productRepository.findById(productions.getProduct().getId());
                    products.add(optionalProduct.get());
                } else {
                    Optional<ProductTypePrice> optionalProductTypePrice = productTypePriceRepository.findById(productions.getProductTypePrice().getId());
                    priceList.add(optionalProductTypePrice.get());
                }
            }
            productList = products;
            productTypePriceList = priceList;
        } else if (brandId != null && categoryId != null) {
            productList = productRepository.findAllByBrandIdAndCategoryIdAndBranchIdAndActiveTrue(brandId, categoryId, branchId);
            productTypePriceList = productTypePriceRepository.findAllByProduct_BrandIdAndProduct_CategoryIdAndProduct_Branch_IdAndProduct_ActiveIsTrue(brandId,categoryId,branchId);
        } else if (production != null && categoryId == null && brandId == null) {
            List<Production> productionList = productionRepository.findAllByBranchId(branchId);
            if (productionList.isEmpty()) {
                return new ApiResponse("Production Not Found", false);
            }
            List<Product> products = new ArrayList<>();
            List<ProductTypePrice> priceList = new ArrayList<>();
            for (Production productions : productionList) {
                if (productions.getProduct() != null) {
                    Optional<Product> optionalProduct = productRepository.findById(productions.getProduct().getId());
                    products.add(optionalProduct.get());
                } else {
                    Optional<ProductTypePrice> optionalProductTypePrice = productTypePriceRepository.findById(productions.getProductTypePrice().getId());
                    priceList.add(optionalProductTypePrice.get());
                }
            }
            productList = products;
            productTypePriceList = priceList;
        }
        if (productList.isEmpty() && productTypePriceList.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }

        double SumBySalePrice = 0;
        double SumByBuyPrice = 0;

        List<ProductReportDto> productReportDtoList = new ArrayList<>();
        ProductReportDto productReportDto = new ProductReportDto();
        for (Product product : productList) {
            productReportDto = new ProductReportDto();
            productReportDto.setName(product.getName());
            if (product.getBrand() != null) productReportDto.setBrand(product.getBrand().getName());
            productReportDto.setBranch(optionalBranch.get().getName());
            if (product.getCategory() != null) productReportDto.setCategory(product.getCategory().getName());
            if (product.getChildCategory() != null) productReportDto.setCategory(product.getChildCategory().getName());
            productReportDto.setBuyPrice(product.getBuyPrice());
            productReportDto.setSalePrice(product.getSalePrice());

            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByProductIdAndBranchId(product.getId(), optionalBranch.get().getId());
            Warehouse warehouse = new Warehouse();
            if (optionalWarehouse.isPresent()) {
                warehouse = optionalWarehouse.get();
            }
            productReportDto.setAmount(warehouse.getAmount());

            double amount = warehouse.getAmount();
            double salePrice = product.getSalePrice();
            double buyPrice = product.getBuyPrice();

            SumBySalePrice = amount * salePrice;
            SumByBuyPrice = amount * buyPrice;
            productReportDto.setSumBySalePrice(SumBySalePrice);
            productReportDto.setSumByBuyPrice(SumByBuyPrice);
            if (productReportDto.getBarcode() == null && productReportDto.getAmount() == 0 && productReportDto.getBuyPrice() == 0 && productReportDto.getSalePrice() == 0)
                continue;
            productReportDtoList.add(productReportDto);
        }

        for (ProductTypePrice productTypePrice : productTypePriceList) {
            productReportDto = new ProductReportDto();
            productReportDto.setName(productTypePrice.getName());
            if (productTypePrice.getProduct().getBrand() != null)
                productReportDto.setBrand(productTypePrice.getProduct().getBrand().getName());
            productReportDto.setBranch(optionalBranch.get().getName());
            if (productTypePrice.getProduct().getCategory() != null)
                productReportDto.setCategory(productTypePrice.getProduct().getCategory().getName());
            if (productTypePrice.getProduct().getChildCategory() != null)
                productReportDto.setChildCategory(productTypePrice.getProduct().getChildCategory().getName());
            productReportDto.setBuyPrice(productTypePrice.getBuyPrice());
            productReportDto.setSalePrice(productTypePrice.getSalePrice());

            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByProductTypePriceIdAndBranchId(productTypePrice.getId(), branchId);
            Warehouse warehouse = new Warehouse();
            if (optionalWarehouse.isPresent()) {
                warehouse = optionalWarehouse.get();
            }
            productReportDto.setAmount(warehouse.getAmount());

            double amount = warehouse.getAmount();
            double salePrice = productTypePrice.getSalePrice();
            double buyPrice = productTypePrice.getBuyPrice();

            SumBySalePrice = amount * salePrice;
            SumByBuyPrice = amount * buyPrice;
            productReportDto.setSumBySalePrice(SumBySalePrice);
            productReportDto.setSumByBuyPrice(SumByBuyPrice);
            productReportDtoList.add(productReportDto);
        }
        productReportDtoList.sort(Comparator.comparing(ProductReportDto::getAmount).reversed());
        return new ApiResponse("Business Products Amount", true, productReportDtoList);
    }

    public ApiResponse tradeProductByBranch(UUID branchId, UUID payMethodId, UUID customerId, Date startDate, Date endDate) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }

        List<TradeProduct> tradeProductList = new ArrayList<>();
        if (payMethodId == null && customerId == null && startDate == null && endDate == null) {
            tradeProductList = tradeProductRepository.findAllByTrade_BranchId(branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (payMethodId != null && customerId == null && startDate == null && endDate == null) {
            tradeProductList = tradeProductRepository.findAllByTrade_PayMethodIdAndTrade_BranchId(payMethodId, branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (payMethodId != null && customerId == null && startDate != null && endDate != null) {
            Timestamp from = new Timestamp(startDate.getTime());
            Timestamp to = new Timestamp(endDate.getTime());
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_PayMethodIdAndTrade_BranchId(from, to, payMethodId, branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (customerId != null && payMethodId == null && startDate != null && endDate != null) {
            Timestamp from = new Timestamp(startDate.getTime());
            Timestamp to = new Timestamp(endDate.getTime());
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_CustomerIdAndTrade_BranchId(from, to, customerId, branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (customerId == null && payMethodId == null && startDate != null && endDate != null) {
            Timestamp from = new Timestamp(startDate.getTime());
            Timestamp to = new Timestamp(endDate.getTime());
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchId(from, to, branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (payMethodId == null && customerId != null && startDate == null && endDate == null) {
            tradeProductList = tradeProductRepository.findAllByTrade_CustomerIdAndTrade_BranchId(customerId, branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (customerId != null && payMethodId != null && startDate == null && endDate == null) {
            tradeProductList = tradeProductRepository.findAllByTrade_CustomerIdAndTrade_BranchIdAndTrade_PayMethodId(customerId, branchId, payMethodId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        }

        List<TradeReportsDto> tradeReportsDtoList = new ArrayList<>();
        for (TradeProduct tradeProduct : tradeProductList) {
            TradeReportsDto tradeReportsDto = new TradeReportsDto();
            if (tradeProduct.getProduct() != null) {
                tradeReportsDto.setName(tradeProduct.getProduct().getName());
                tradeReportsDto.setBarcode(tradeProduct.getProduct().getBarcode());
                tradeReportsDto.setSalePrice(tradeProduct.getProduct().getSalePrice());
            } else {
                tradeReportsDto.setName(tradeProduct.getProductTypePrice().getName());
                tradeReportsDto.setBarcode(tradeProduct.getProductTypePrice().getBarcode());
                tradeReportsDto.setSalePrice(tradeProduct.getProductTypePrice().getSalePrice());
            }

            tradeReportsDto.setTradeProductId(tradeProduct.getTrade().getId());
            tradeReportsDto.setTradedDate(tradeProduct.getTrade().getPayDate());

            if (tradeProduct.getTrade().getCustomer() != null) {
                tradeReportsDto.setCustomerName(tradeProduct.getTrade().getCustomer().getName());
            }
            tradeReportsDto.setPayMethod(tradeProduct.getTrade().getPayMethod().getType());
            tradeReportsDto.setAmount(tradeProduct.getTradedQuantity());
            if (tradeProduct.getTrade().getCustomer() != null) {
                tradeReportsDto.setDiscount(tradeProduct.getTrade().getCustomer().getCustomerGroup().getPercent());
            }
            tradeReportsDto.setTotalSum(tradeProduct.getTotalSalePrice());
            tradeReportsDtoList.add(tradeReportsDto);
        }
        tradeReportsDtoList.sort(Comparator.comparing(TradeReportsDto::getAmount).reversed());
        return new ApiResponse("Traded Products", true, tradeReportsDtoList);
    }

    public ApiResponse allProductByBrand(UUID branchId, UUID brandId) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        Optional<Brand> optionalBrand = brandRepository.findById(brandId);
        if (optionalBrand.isEmpty()) {
            return new ApiResponse("Brand Not Found", false);
        }

        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }
        Optional<Business> optionalBusiness = businessRepository.findById(optionalBranch.get().getBusiness().getId());

        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("Business Not Found", false);
        }
        UUID businessId = optionalBranch.get().getBusiness().getId();
        List<Product> productList = productRepository.findAllByBrandIdAndBusinessIdAndActiveTrue(brandId, businessId);
        List<ProductTypePrice> productTypePrices = productTypePriceRepository.findAllByProduct_BranchIdAndProduct_BrandId(branchId,brandId);
        if (productList.isEmpty() && productTypePrices.isEmpty()) {
            return new ApiResponse("No Found Products", false);
        }

        double SumBySalePrice = 0;
        double SumByBuyPrice = 0;

        List<ProductReportDto> productReportDtoList = new ArrayList<>();
        ProductReportDto productReportDto = new ProductReportDto();
        for (Product product : productList) {
            productReportDto = new ProductReportDto();
            productReportDto.setName(product.getName());
            if (product.getBrand() != null) productReportDto.setBrand(product.getBrand().getName());
            productReportDto.setBranch(optionalBranch.get().getName());
            if (product.getCategory() != null) productReportDto.setCategory(product.getCategory().getName());
            if (product.getChildCategory() != null) productReportDto.setChildCategory(product.getChildCategory().getName());
            productReportDto.setBuyPrice(product.getBuyPrice());
            productReportDto.setSalePrice(product.getSalePrice());

            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByProductIdAndBranchId(product.getId(), optionalBranch.get().getId());
            Warehouse warehouse = new Warehouse();
            if (optionalWarehouse.isPresent()) {
                warehouse = optionalWarehouse.get();
            }
            productReportDto.setAmount(warehouse.getAmount());

            double amount = warehouse.getAmount();
            double salePrice = product.getSalePrice();
            double buyPrice = product.getBuyPrice();

            SumBySalePrice = amount * salePrice;
            SumByBuyPrice = amount * buyPrice;
            productReportDto.setSumBySalePrice(SumBySalePrice);
            productReportDto.setSumByBuyPrice(SumByBuyPrice);
            if (productReportDto.getSalePrice() == 0 && productReportDto.getAmount() == 0 && productReportDto.getBuyPrice() == 0 && productReportDto.getBarcode()== null)
                continue;
            productReportDtoList.add(productReportDto);
        }
        for (ProductTypePrice product : productTypePrices) {
            productReportDto = new ProductReportDto();
            productReportDto.setName(product.getName());
            if (product.getProduct().getBrand() != null)
                productReportDto.setBrand(product.getProduct().getBrand().getName());
            productReportDto.setBranch(optionalBranch.get().getName());
            if (product.getProduct().getCategory() != null)
                productReportDto.setCategory(product.getProduct().getCategory().getName());
            if (product.getProduct().getChildCategory() != null)
                productReportDto.setChildCategory(product.getProduct().getChildCategory().getName());
            productReportDto.setBuyPrice(product.getBuyPrice());
            productReportDto.setSalePrice(product.getSalePrice());

            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(optionalBranch.get().getId(), product.getId());
            Warehouse warehouse = new Warehouse();
            if (optionalWarehouse.isPresent()) {
                warehouse = optionalWarehouse.get();
            }
            productReportDto.setAmount(warehouse.getAmount());

            double amount = warehouse.getAmount();
            double salePrice = product.getSalePrice();
            double buyPrice = product.getBuyPrice();

            SumBySalePrice = amount * salePrice;
            SumByBuyPrice = amount * buyPrice;
            productReportDto.setSumBySalePrice(SumBySalePrice);
            productReportDto.setSumByBuyPrice(SumByBuyPrice);
            productReportDtoList.add(productReportDto);
        }
        productReportDtoList.sort(Comparator.comparing(ProductReportDto::getAmount).reversed());
        return new ApiResponse("Business Products Amount", true, productReportDtoList);
    }

    public ApiResponse allProductByCategory(UUID branchId, UUID categoryId) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isEmpty()) {
            return new ApiResponse("Category Not Found", false);
        }
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }
        Optional<Business> optionalBusiness = businessRepository.findById(optionalBranch.get().getBusiness().getId());

        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("Business Not Found", false);
        }
        UUID businessId = optionalBranch.get().getBusiness().getId();
        List<Product> productList = productRepository.findAllByCategoryIdAndBusinessIdAndActiveTrue(categoryId, businessId);
        List<ProductTypePrice> productTypePriceList = productTypePriceRepository.findAllByProduct_CategoryIdAndProduct_BranchIdAndProduct_ActiveTrue(categoryId, branchId);
        if (productList.isEmpty() && productTypePriceList.isEmpty()) {
            return new ApiResponse("No Found Products", false);
        }

        double SumBySalePrice = 0;
        double SumByBuyPrice = 0;

        List<ProductReportDto> productReportDtoList = new ArrayList<>();
        ProductReportDto productReportDto;
        for (Product product : productList) {
            productReportDto = new ProductReportDto();
            productReportDto.setName(product.getName());
            if (product.getBrand() != null) productReportDto.setBrand(product.getBrand().getName());
            productReportDto.setBranch(optionalBranch.get().getName());
            if (product.getCategory() != null) productReportDto.setCategory(product.getCategory().getName());
            if (product.getChildCategory() != null) productReportDto.setChildCategory(product.getChildCategory().getName());
            productReportDto.setBuyPrice(product.getBuyPrice());
            productReportDto.setSalePrice(product.getSalePrice());

            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByProductIdAndBranchId(product.getId(), optionalBranch.get().getId());
            Warehouse warehouse = new Warehouse();
            if (optionalWarehouse.isPresent()) {
                warehouse = optionalWarehouse.get();
            }
            productReportDto.setAmount(warehouse.getAmount());

            double amount = warehouse.getAmount();
            double salePrice = product.getSalePrice();
            double buyPrice = product.getBuyPrice();

            SumBySalePrice = amount * salePrice;
            SumByBuyPrice = amount * buyPrice;
            productReportDto.setSumBySalePrice(SumBySalePrice);
            productReportDto.setSumByBuyPrice(SumByBuyPrice);
            if (productReportDto.getBarcode() == null && productReportDto.getBuyPrice() == 0 && productReportDto.getSalePrice() == 0 && productReportDto.getAmount() == 0)
                continue;
            productReportDtoList.add(productReportDto);
        }
        for (ProductTypePrice product : productTypePriceList) {
            productReportDto = new ProductReportDto();
            productReportDto.setName(product.getName());
            if (product.getProduct().getBrand() != null)
                productReportDto.setBrand(product.getProduct().getBrand().getName());
            productReportDto.setBranch(optionalBranch.get().getName());
            if (product.getProduct().getCategory() != null)
                productReportDto.setCategory(product.getProduct().getCategory().getName());
            if (product.getProduct().getChildCategory() != null)
                productReportDto.setChildCategory(product.getProduct().getChildCategory().getName());
            productReportDto.setBuyPrice(product.getBuyPrice());
            productReportDto.setSalePrice(product.getSalePrice());
            productReportDto.setBrand(product.getBarcode());
            productReportDto.setBarcode(product.getBarcode());

            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branchId,product.getId());
            Warehouse warehouse = new Warehouse();
            if (optionalWarehouse.isPresent()) {
                warehouse = optionalWarehouse.get();
            }
            productReportDto.setAmount(warehouse.getAmount());

            double amount = warehouse.getAmount();
            double salePrice = product.getSalePrice();
            double buyPrice = product.getBuyPrice();

            SumBySalePrice = amount * salePrice;
            SumByBuyPrice = amount * buyPrice;
            productReportDto.setSumBySalePrice(SumBySalePrice);
            productReportDto.setSumByBuyPrice(SumByBuyPrice);
            productReportDtoList.add(productReportDto);
        }
        productReportDtoList.sort(Comparator.comparing(ProductReportDto::getAmount).reversed());
        return new ApiResponse("Business Products Amount", true, productReportDtoList);
    }

    public ApiResponse allProductAmountByBranch(UUID branchId) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);

        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }
        List<Product> productList = productRepository.findAllByBranchIdAndActiveTrue(branchId);
        List<ProductTypePrice> productTypePriceList = productTypePriceRepository.findAllByProduct_BranchId(branchId);

        if (productList.isEmpty() && productTypePriceList.isEmpty()) {
            return new ApiResponse("No Found Products", false);
        }
        double totalSumBySalePrice = 0D;
        double totalSumByBuyPrice = 0D;
        Amount amounts = new Amount();
        for (Product product : productList) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByProductIdAndBranchId(product.getId(), optionalBranch.get().getId());
            if (optionalWarehouse.isPresent()) {
                double amount = optionalWarehouse.get().getAmount();
                double salePrice = product.getSalePrice();
                double buyPrice = product.getBuyPrice();

                totalSumBySalePrice += amount * salePrice;
                totalSumByBuyPrice += amount * buyPrice;
                amounts.setTotalSumBySalePrice(totalSumBySalePrice);
                amounts.setTotalSumByBuyPrice(totalSumByBuyPrice);
            }
        }
        for (ProductTypePrice product : productTypePriceList) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branchId,product.getId());
            if (optionalWarehouse.isPresent()) {
                double amount = optionalWarehouse.get().getAmount();
                double salePrice = product.getSalePrice();
                double buyPrice = product.getBuyPrice();

                totalSumBySalePrice += amount * salePrice;
                totalSumByBuyPrice += amount * buyPrice;
                amounts.setTotalSumBySalePrice(totalSumBySalePrice);
                amounts.setTotalSumByBuyPrice(totalSumByBuyPrice);
            }
        }
        return new ApiResponse("Business Products Amount", true, amounts);
    }

    public ApiResponse mostUnSaleProducts(UUID branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found");
        }
        List<TradeProduct> tradeProductList = tradeProductRepository.findAllByTrade_BranchId(branchId);

        if (tradeProductList.isEmpty()) {
            return new ApiResponse("Traded Product Not Found");
        }

        Map<UUID, Double> productAmount = new HashMap<>();
        List<TradeProduct> allByProductId = new ArrayList<>();
        for (TradeProduct tradeProduct : tradeProductList) {
            double amount = 0;
            if (tradeProduct.getProductTypePrice() != null) {
                List<TradeProduct> tradeProducts = tradeProductRepository.findAllByTrade_BranchIdAndProductTypePriceId(tradeProduct.getTrade().getBranch().getId(), tradeProduct.getProductTypePrice().getId());
                if (tradeProduct.getProductTypePrice() != null) {
                    for (TradeProduct product : tradeProducts) {
                        amount += product.getTradedQuantity();
                        productAmount.put(product.getProductTypePrice().getId(), amount);
                    }
                }
            }
            if (tradeProduct.getProduct() != null) {
                List<TradeProduct> productList = tradeProductRepository.findAllByTrade_BranchIdAndProduct_Id(tradeProduct.getTrade().getBranch().getId(), tradeProduct.getProduct().getId());
                if (tradeProduct.getProduct() != null) {
                    for (TradeProduct product : productList) {
                        amount += product.getTradedQuantity();
                        productAmount.put(product.getProduct().getId(), amount);
                    }
                }
            }
        }
            List<MostSaleProductsDto> mostSaleProductsDtoList = new ArrayList<>();
            for (Map.Entry<UUID, Double> entry : productAmount.entrySet()) {
            MostSaleProductsDto mostSaleProductsDto = new MostSaleProductsDto();
            Optional<Product> product = productRepository.findById(entry.getKey());
            if (product.isPresent()) {
                mostSaleProductsDto.setName(product.get().getName());
                mostSaleProductsDto.setAmount(entry.getValue());
                mostSaleProductsDto.setSalePrice(product.get().getSalePrice());
                mostSaleProductsDto.setBuyPrice(product.get().getBuyPrice());
                mostSaleProductsDto.setBarcode(product.get().getBarcode());
                mostSaleProductsDto.setMeasurement(product.get().getMeasurement().getName());
                mostSaleProductsDto.setBranchName(optionalBranch.get().getName());
                mostSaleProductsDtoList.add(mostSaleProductsDto);
            }
            Optional<ProductTypePrice> productTypePrice = productTypePriceRepository.findById(entry.getKey());
            if (productTypePrice.isPresent()) {
                mostSaleProductsDto.setName(productTypePrice.get().getName());
                mostSaleProductsDto.setAmount(entry.getValue());
                mostSaleProductsDto.setSalePrice(productTypePrice.get().getSalePrice());
                mostSaleProductsDto.setBuyPrice(productTypePrice.get().getBuyPrice());
                mostSaleProductsDto.setBarcode(productTypePrice.get().getBarcode());
                mostSaleProductsDto.setMeasurement(productTypePrice.get().getProduct().getMeasurement().getName());
                mostSaleProductsDto.setBranchName(optionalBranch.get().getName());
                mostSaleProductsDtoList.add(mostSaleProductsDto);
            }

    }
        mostSaleProductsDtoList.sort(Comparator.comparing(MostSaleProductsDto::getAmount));
        return new ApiResponse("Found", true, mostSaleProductsDtoList);
    }

    public ApiResponse purchaseReports(UUID branchId, UUID supplierId, Date startDate, Date endDate) {
        Timestamp end = null;
        Timestamp start = null;
        List<PurchaseProduct> purchaseProductList = new ArrayList<>();
        List<PurchaseReportsDto> purchaseReportsDtoList = new ArrayList<>();

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }
        Branch branch = optionalBranch.get();

        if (startDate != null && endDate != null) {
            end = new Timestamp(endDate.getTime());
            start = new Timestamp(startDate.getTime());
        }

        if (supplierId != null) {
            if (end != null) {
                purchaseProductList = purchaseProductRepository.findAllByCreatedAtBetweenAndProduct_BranchIdAndPurchase_SupplierId(start, end, branchId, supplierId);
            } else {
                purchaseProductList = purchaseProductRepository.findAllByPurchase_BranchIdAndPurchase_SupplierId(branchId, supplierId);
            }
        } else {
            if (end != null) {
                purchaseProductList = purchaseProductRepository.findAllByCreatedAtBetweenAndPurchase_BranchId(start, end, branchId);
            } else {
                purchaseProductList = purchaseProductRepository.findAllByPurchase_BranchId(branch.getId());
            }
        }
        if (purchaseProductList.isEmpty()) {
            return new ApiResponse("Purchase Not Found", false);
        }
        for (PurchaseProduct purchaseProduct : purchaseProductList) {
            if (purchaseProduct.getProduct()==null){
                PurchaseReportsDto purchaseReportsDto = new PurchaseReportsDto();
                purchaseReportsDto.setPurchaseId(purchaseProduct.getPurchase().getId());
                purchaseReportsDto.setPurchasedAmount(purchaseProduct.getPurchasedQuantity());
                purchaseReportsDto.setName(purchaseProduct.getProductTypePrice().getName());
                purchaseReportsDto.setBuyPrice(purchaseProduct.getProductTypePrice().getBuyPrice());
                purchaseReportsDto.setBarcode(purchaseProduct.getProductTypePrice().getBarcode());
                purchaseReportsDto.setTax(purchaseProduct.getProductTypePrice().getProfitPercent());
                purchaseReportsDto.setTotalSum(purchaseProduct.getTotalSum());
                purchaseReportsDto.setPurchasedDate(purchaseProduct.getCreatedAt());
                purchaseReportsDto.setSupplier(purchaseProduct.getPurchase().getSupplier().getName());
                purchaseReportsDto.setDebt(purchaseProduct.getPurchase().getDebtSum());
                purchaseReportsDtoList.add(purchaseReportsDto);
            }else {
                PurchaseReportsDto purchaseReportsDto = new PurchaseReportsDto();
                purchaseReportsDto.setPurchaseId(purchaseProduct.getPurchase().getId());
                purchaseReportsDto.setPurchasedAmount(purchaseProduct.getPurchasedQuantity());
                purchaseReportsDto.setName(purchaseProduct.getProduct().getName());
                purchaseReportsDto.setBuyPrice(purchaseProduct.getBuyPrice());
                purchaseReportsDto.setBarcode(purchaseProduct.getProduct().getBarcode());
                purchaseReportsDto.setTax(purchaseProduct.getProduct().getTax());
                purchaseReportsDto.setTotalSum(purchaseProduct.getTotalSum());
                purchaseReportsDto.setPurchasedDate(purchaseProduct.getCreatedAt());
                purchaseReportsDto.setSupplier(purchaseProduct.getPurchase().getSupplier().getName());
                purchaseReportsDto.setDebt(purchaseProduct.getPurchase().getDebtSum());
                purchaseReportsDtoList.add(purchaseReportsDto);
            }
        }
        return new ApiResponse("Found", true, purchaseReportsDtoList);
    }

    public ApiResponse deliveryPriceGet(UUID branchId) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Not Found");
        }

        List<Purchase> purchaseList = purchaseRepository.findAllByBranch_Id(branchId);

        if (purchaseList.isEmpty()) {
            return new ApiResponse("Not Found Purchase", false);
        }

        double totalDelivery = 0;
        for (Purchase purchase : purchaseList) {
            totalDelivery += purchase.getDeliveryPrice();
        }
        return new ApiResponse("Found", true, totalDelivery);
    }

    public ApiResponse outlayReports(UUID branchId, UUID categoryId, Date startDate, Date endDate) {
        List<Outlay> outlayList = new ArrayList<>();
        Timestamp start = null;
        Timestamp end = null;

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }

        if (startDate != null && endDate != null) {
            start = new Timestamp(startDate.getTime());
            end = new Timestamp(endDate.getTime());
        }

        if (categoryId == null && startDate == null && endDate == null) {
            outlayCategoryRepository.findAllByBranch_Id(branchId);
            outlayList = outlayRepository.findAllByBranch_Id(branchId);
            if (outlayList.isEmpty()) {
                return new ApiResponse("Not Found Outlay", false);
            }
        } else if (categoryId != null && startDate == null && endDate == null) {
            outlayList = outlayRepository.findAllByBranch_IdAndOutlayCategoryId(branchId, categoryId);
            if (outlayList.isEmpty()) {
                return new ApiResponse("Not Found Outlay", false);
            }
        } else if (categoryId != null && startDate != null && endDate != null) {
            outlayList = outlayRepository.findAllByCreatedAtBetweenAndBranchIdAndOutlayCategoryId(start, end, branchId, categoryId);
            if (outlayList.isEmpty()) {
                return new ApiResponse("Not Found Outlay", false);
            }
        } else if (categoryId == null && startDate != null && endDate != null) {
            outlayList = outlayRepository.findAllByCreatedAtBetweenAndBranchId(start, end, branchId);
            if (outlayList.isEmpty()) {
                return new ApiResponse("Not Found Outlay", false);
            }
        }
        Map<UUID, Double> productAmount = new HashMap<>();
        for (Outlay outlay : outlayList) {
            OutlayCategory category = outlay.getOutlayCategory();
            double totalSum = productAmount.getOrDefault(category.getId(), 0.0);
            totalSum += outlay.getTotalSum();
            productAmount.put(category.getId(), totalSum);
        }
        Map<String, Double> outlays = new HashMap<>();
        List<Outlay> all;
        for (Outlay outlay : outlayList) {
            if (startDate != null) {
                all = outlayRepository.findAllByCreatedAtBetweenAndBranchIdAndOutlayCategoryId(start, end, branchId, outlay.getOutlayCategory().getId());
            } else {
                all = outlayRepository.findAllByBranch_IdAndOutlayCategoryId(branchId, outlay.getOutlayCategory().getId());
            }
            double totalsumma = 0;
            for (Outlay outlayByCategory : all) {
                totalsumma += outlayByCategory.getTotalSum();
                outlays.put(outlay.getOutlayCategory().getTitle(), totalsumma);
            }
        }

        List<OutlayGetCategory> outlayGetCategoryList = new ArrayList<>();

        for (Map.Entry<String, Double> entry : outlays.entrySet()) {
            OutlayGetCategory category = new OutlayGetCategory();
            category.setType(entry.getKey());
            category.setTotalSum(entry.getValue());
            outlayGetCategoryList.add(category);
        }

        outlayGetCategoryList.sort(Comparator.comparing(OutlayGetCategory::getTotalSum));
        return new ApiResponse("Found", true, outlayGetCategoryList);
    }

    public ApiResponse customerReports(UUID branchId, UUID customerId, Date startDate, Date endDate) {
        Timestamp to = null;
        Timestamp from = null;

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        List<TradeProduct> tradeProducts = tradeProductRepository.findAllByTrade_BranchId(branchId);
        if (tradeProducts.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }

        if (startDate != null && endDate != null) {
            to = new Timestamp(endDate.getTime());
            from = new Timestamp(startDate.getTime());
        }

        List<TradeProduct> tradeProductList = new ArrayList<>();
        List<CustomerReportsDto> customerReportsDtoList = new ArrayList<>();

        if (customerId != null) {
            if (from != null) {
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_Customer_Id(from, to, customerId);
            } else {
                tradeProductList = tradeProductRepository.findAllByTrade_CustomerId(customerId);
            }
        } else {
            if (from != null) {
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchId(from, to, branchId);
            } else {
                tradeProductList = tradeProductRepository.findAllByTrade_BranchId(branchId);
            }
        }

        if (tradeProductList.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }

        for (TradeProduct tradeProduct : tradeProductList) {
            if (tradeProduct.getProduct() != null) {
                CustomerReportsDto customerReportsDto = new CustomerReportsDto();
                if (tradeProduct.getTrade().getCustomer() == null)
                    continue;
                customerReportsDto.setCustomerName(tradeProduct.getTrade().getCustomer().getName());
                customerReportsDto.setDate(tradeProduct.getTrade().getPayDate());
                customerReportsDto.setDebt(tradeProduct.getTrade().getDebtSum());
                customerReportsDto.setProduct(tradeProduct.getProduct().getName());
                customerReportsDto.setPaidSum(tradeProduct.getTrade().getPaidSum());
                customerReportsDto.setTradedQuantity(tradeProduct.getTradedQuantity());
                customerReportsDto.setBranchName(tradeProduct.getTrade().getBranch().getName());
                customerReportsDto.setTotalSum(tradeProduct.getTrade().getTotalSum());
                customerReportsDto.setPayMethod(tradeProduct.getTrade().getPayMethod().getType());
                customerReportsDto.setPaymentStatus(tradeProduct.getTrade().getPaymentStatus().getStatus());
                customerReportsDtoList.add(customerReportsDto);
            }else {
                CustomerReportsDto customerReportsDto = new CustomerReportsDto();
                if (tradeProduct.getTrade().getCustomer() == null)
                    continue;
                customerReportsDto.setCustomerName(tradeProduct.getTrade().getCustomer().getName());
                customerReportsDto.setDate(tradeProduct.getTrade().getPayDate());
                customerReportsDto.setDebt(tradeProduct.getTrade().getDebtSum());
                customerReportsDto.setProduct(tradeProduct.getProductTypePrice().getName());
                customerReportsDto.setPaidSum(tradeProduct.getTrade().getPaidSum());
                customerReportsDto.setTradedQuantity(tradeProduct.getTradedQuantity());
                customerReportsDto.setBranchName(tradeProduct.getTrade().getBranch().getName());
                customerReportsDto.setTotalSum(tradeProduct.getTrade().getTotalSum());
                customerReportsDto.setPayMethod(tradeProduct.getTrade().getPayMethod().getType());
                customerReportsDto.setPaymentStatus(tradeProduct.getTrade().getPaymentStatus().getStatus());
                customerReportsDtoList.add(customerReportsDto);
            }
        }

        customerReportsDtoList.sort(Comparator.comparing(CustomerReportsDto::getTotalSum).reversed());
        return new ApiResponse("Found", true, customerReportsDtoList);
    }

    public ApiResponse mostSaleProducts(UUID branchId, UUID categoryId, UUID brandId, Date startDate, Date endDate) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }

        List<TradeProduct> tradeProductList = new ArrayList<>();

        if (categoryId == null && brandId == null && startDate == null && endDate == null) {
            tradeProductList = tradeProductRepository.findAllByTrade_BranchId(branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (categoryId != null && brandId == null && startDate == null && endDate == null) {
            tradeProductList = tradeProductRepository.findAllByProduct_CategoryIdAndTrade_BranchId(categoryId, branchId);
            tradeProductList = tradeProductRepository.findAllByProductTypePrice_Product_CategoryIdAndTrade_BranchId(categoryId,branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (categoryId == null && brandId != null && startDate == null && endDate == null) {
            tradeProductList = tradeProductRepository.findAllByProduct_BrandIdAndTrade_BranchId(brandId, branchId);
            tradeProductList = tradeProductRepository.findAllByProductTypePrice_Product_BrandIdAndTrade_BranchId(brandId,branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (categoryId == null && brandId == null && startDate != null && endDate != null) {
            Timestamp from = new Timestamp(startDate.getTime());
            Timestamp to = new Timestamp(endDate.getTime());
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchId(from, to, branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (categoryId != null && brandId == null && startDate != null && endDate != null) {
            Timestamp from = new Timestamp(startDate.getTime());
            Timestamp to = new Timestamp(endDate.getTime());
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchIdAndProduct_CategoryId(from, to, branchId, categoryId);
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchIdAndProductTypePrice_Product_CategoryId(from, to, branchId, categoryId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (categoryId == null && brandId != null && startDate != null && endDate != null) {
            Timestamp from = new Timestamp(startDate.getTime());
            Timestamp to = new Timestamp(endDate.getTime());
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchIdAndProduct_BrandId(from, to, branchId, brandId);
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchIdAndProductTypePrice_Product_BrandId(from, to, branchId, brandId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (categoryId != null && brandId != null && startDate == null && endDate == null) {
            tradeProductList = tradeProductRepository.findAllByProduct_CategoryIdAndProduct_BrandIdAndTrade_BranchId(categoryId, brandId, branchId);
            tradeProductList = tradeProductRepository.findAllByProductTypePrice_Product_CategoryIdAndProductTypePrice_Product_BrandIdAndTrade_BranchId(categoryId, brandId, branchId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        } else if (categoryId != null && brandId != null && startDate != null && endDate != null) {
            Timestamp from = new Timestamp(startDate.getTime());
            Timestamp to = new Timestamp(endDate.getTime());
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchIdAndProduct_CategoryIdAndProduct_BrandId(from, to, branchId, categoryId, brandId);
            tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchIdAndProductTypePrice_Product_CategoryIdAndProductTypePrice_Product_BrandId(from, to, branchId, categoryId, brandId);
            if (tradeProductList.isEmpty()) {
                return new ApiResponse("Trade Not Found", false);
            }
        }

        Map<UUID, Double> productAmount = new HashMap<>();
        List<TradeProduct> allByProductId = new ArrayList<>();

        for (TradeProduct tradeProduct : tradeProductList) {

            List<TradeProduct> tradeProducts = new ArrayList<>();
            if (tradeProduct.getProduct() != null){
                allByProductId = tradeProductRepository.findAllByProduct_Id(tradeProduct.getProduct().getId());
            }else {
                tradeProducts = tradeProductRepository.findAllByTrade_BranchIdAndProductTypePriceId(branchId,tradeProduct.getProductTypePrice().getId());
            }
            double amount = 0;
            if (tradeProduct.getProduct() == null){
                for (TradeProduct product : tradeProducts) {
                    amount += product.getTradedQuantity();
                    productAmount.put(product.getProductTypePrice().getId(), amount);
                }
            }
            for (TradeProduct product : allByProductId) {
                amount += product.getTradedQuantity();
                productAmount.put(product.getProduct().getId(), amount);
            }
        }
        List<MostSaleProductsDto> mostSaleProductsDtoList = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : productAmount.entrySet()) {

            Optional<Product> product = productRepository.findById(entry.getKey());
            if (product.isPresent()){
                MostSaleProductsDto mostSaleProductsDto = new MostSaleProductsDto();
                mostSaleProductsDto.setName(product.get().getName());
                mostSaleProductsDto.setAmount(entry.getValue());
                mostSaleProductsDto.setSalePrice(product.get().getSalePrice());
                mostSaleProductsDto.setBuyPrice(product.get().getBuyPrice());
                mostSaleProductsDto.setBarcode(product.get().getBarcode());
                mostSaleProductsDto.setMeasurement(product.get().getMeasurement().getName());
                mostSaleProductsDto.setBranchName(optionalBranch.get().getName());
                mostSaleProductsDtoList.add(mostSaleProductsDto);
            }else {
                Optional<ProductTypePrice> productTypePrice = productTypePriceRepository.findById(entry.getKey());
                MostSaleProductsDto mostSaleProductsDto = new MostSaleProductsDto();
                mostSaleProductsDto.setName(productTypePrice.get().getName());
                mostSaleProductsDto.setAmount(entry.getValue());
                mostSaleProductsDto.setSalePrice(productTypePrice.get().getSalePrice());
                mostSaleProductsDto.setBuyPrice(productTypePrice.get().getBuyPrice());
                mostSaleProductsDto.setBarcode(productTypePrice.get().getBarcode());
                mostSaleProductsDto.setMeasurement(productTypePrice.get().getProduct().getMeasurement().getName());
                mostSaleProductsDto.setBranchName(optionalBranch.get().getName());
                mostSaleProductsDtoList.add(mostSaleProductsDto);
            }


        }
        mostSaleProductsDtoList.sort(Comparator.comparing(MostSaleProductsDto::getAmount).reversed());
        return new ApiResponse("Found", true, mostSaleProductsDtoList);
    }

    public ApiResponse dateBenefitAndLostByProductReports(UUID branchId, String date, Date comingStartDate, Date comingEndDate) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }

        List<TradeProduct> tradeBranchId = tradeProductRepository.findAllByTrade_BranchId(optionalBranch.get().getId());
        if (tradeBranchId.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        Map<UUID, Double> productAmount = new HashMap<>();
        for (TradeProduct tradeProduct : tradeBranchId) {
            double amount = 0;
            if (Objects.equals(date, "LAST_DAY")) {
                if (tradeProduct.getProduct() == null){
                    List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePriceId(Timestamp.valueOf(START_OF_DAY), Timestamp.valueOf(END_OF_DAY), tradeProduct.getProductTypePrice().getId());
                    if (allByProductId.isEmpty()){
                        return new ApiResponse("Not Found ",false);
                    }
                    for (TradeProduct product : allByProductId) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProductTypePrice().getId(), amount);
                    }
                }else {
                    List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndProductId(Timestamp.valueOf(START_OF_DAY), Timestamp.valueOf(END_OF_DAY), tradeProduct.getProduct().getId());
                    if (allByProductId.isEmpty()){
                        return new ApiResponse("Not Found ",false);
                    }
                    for (TradeProduct product : allByProductId) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProduct().getId(), amount);
                    }
                }
            } else if (Objects.equals(date, "LAST_WEEK")) {
                List<TradeProduct> tradeProductList;
                if (tradeProduct.getProduct() == null){
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePriceId(Timestamp.valueOf(WEEK_START_DAY.atStartOfDay()), Timestamp.valueOf(WEEK_END_DAY.atStartOfDay()), tradeProduct.getProductTypePrice().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProductTypePrice().getId(), amount);
                    }
                }else {
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductId(Timestamp.valueOf(WEEK_START_DAY.atStartOfDay()), Timestamp.valueOf(WEEK_END_DAY.atStartOfDay()), tradeProduct.getProduct().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProduct().getId(), amount);
                    }
                }
                if (tradeProductList.isEmpty()){
                    return new ApiResponse("Not Found",false);
                }
            } else if (Objects.equals(date, "LAST_MONTH")) {
                List<TradeProduct> tradeProductList;
                if (tradeProduct.getProduct() == null){
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePriceId(Timestamp.valueOf(START_OF_MONTH), Timestamp.valueOf(END_OF_MONTH), tradeProduct.getProductTypePrice().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProductTypePrice().getId(), amount);
                    }
                }else {
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductId(Timestamp.valueOf(START_OF_MONTH), Timestamp.valueOf(END_OF_MONTH), tradeProduct.getProduct().getId());
                for (TradeProduct product : tradeProductList) {
                    amount += product.getProfit();
                    productAmount.put(tradeProduct.getProduct().getId(), amount);
                }
                }
                if (tradeProductList.isEmpty()){
                    return new ApiResponse("Not Found",false);
                }
            } else if (Objects.equals(date, "THIS_MONTH")) {
                List<TradeProduct> tradeProductList;
                if (tradeProduct.getProduct() == null){
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePriceId(Timestamp.valueOf(THIS_MONTH), currentDay, tradeProduct.getProductTypePrice().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProductTypePrice().getId(), amount);
                    }
                }else {
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductId(Timestamp.valueOf(THIS_MONTH), currentDay, tradeProduct.getProduct().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProduct().getId(), amount);
                    }
                }
                if (tradeProductList.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
            } else if (Objects.equals(date, "LAST_THIRTY_DAY")) {
                List<TradeProduct> tradeProductList;
                if (tradeProduct.getProduct() == null){
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePriceId(Timestamp.valueOf(LAST_MONTH), currentDay, tradeProduct.getProductTypePrice().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProductTypePrice().getId(), amount);
                    }
                }else {
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductId(Timestamp.valueOf(LAST_MONTH), currentDay, tradeProduct.getProduct().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProduct().getId(), amount);
                    }
                }
                if (tradeProductList.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
            } else if (Objects.equals(date, "THIS_YEAR")) {
                List<TradeProduct> tradeProductList;
                    if (tradeProduct.getProduct() == null){
                        tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePriceId(Timestamp.valueOf(START_OF_YEAR_FOR_THIS), currentDay, tradeProduct.getProductTypePrice().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProductTypePrice().getId(),amount);
                    }
                }else {
                        tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductId(Timestamp.valueOf(START_OF_YEAR_FOR_THIS), currentDay, tradeProduct.getProduct().getId());
                        for (TradeProduct product : tradeProductList) {
                            amount += product.getProfit();
                            productAmount.put(tradeProduct.getProduct().getId(), amount);
                        }
                    }
                if (tradeProductList.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
            } else if (Objects.equals(date, "LAST_YEAR")) {
                List<TradeProduct> tradeProductList;
                if (tradeProduct.getProduct() == null){
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePriceId(Timestamp.valueOf(START_OF_YEAR), Timestamp.valueOf(END_OF_YEAR), tradeProduct.getProductTypePrice().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProductTypePrice().getId(), amount);
                    }
                }else {
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductId(Timestamp.valueOf(START_OF_YEAR), Timestamp.valueOf(END_OF_YEAR), tradeProduct.getProduct().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProduct().getId(), amount);
                    }
                }
                if (tradeProductList.isEmpty()){
                    return new ApiResponse("Not Found",false);
                }
            } else if (comingEndDate != null && comingStartDate != null) {
                List<TradeProduct> tradeProductList;
                Timestamp start = new Timestamp(comingStartDate.getTime());
                Timestamp end = new Timestamp(comingEndDate.getTime());
                if (tradeProduct.getProduct() == null){
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePriceId(start, end, tradeProduct.getProductTypePrice().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProductTypePrice().getId(), amount);
                    }
                }else {
                    tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductId(start, end, tradeProduct.getProduct().getId());
                    if (!tradeProductList.isEmpty()) {
                        for (TradeProduct product : tradeProductList) {
                            amount += product.getProfit();
                            productAmount.put(tradeProduct.getProduct().getId(), amount);
                        }
                    }
                }

                if (tradeProductList.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
            } else {
                List<TradeProduct> tradeProductList = new ArrayList<>();
                if (tradeProduct.getProduct() != null){
                    tradeProductList = tradeProductRepository.findAllByProduct_Id(tradeProduct.getProduct().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProduct().getId(), amount);
                    }
                }else if (tradeProduct.getProductTypePrice() != null){
                    tradeProductList = tradeProductRepository.findAllByProductTypePriceId(tradeProduct.getProductTypePrice().getId());
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(tradeProduct.getProductTypePrice().getId(), amount);
                    }
                }
                if (tradeProductList.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
            }
        }
        List<ProfitByProductDto> profitByProductDtoList = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : productAmount.entrySet()) {
            Optional<Product> optionalProduct = productRepository.findById(entry.getKey());
            if (optionalProduct.isPresent()){
                ProfitByProductDto profitByProductDto = new ProfitByProductDto();
                profitByProductDto.setName(optionalProduct.get().getName());
                profitByProductDto.setProfit(entry.getValue());
                profitByProductDtoList.add(profitByProductDto);
            }
            Optional<ProductTypePrice> productTypePrice = productTypePriceRepository.findById(entry.getKey());
            if (productTypePrice.isPresent()){
                ProfitByProductDto profitByProductDto = new ProfitByProductDto();
                profitByProductDto.setName(productTypePrice.get().getName());
                profitByProductDto.setProfit(entry.getValue());
                profitByProductDtoList.add(profitByProductDto);
            }

        }
        profitByProductDtoList.sort(Comparator.comparing(ProfitByProductDto::getProfit).reversed());

        return new ApiResponse("Found", true, profitByProductDtoList);
    }

    public ApiResponse benefitAndLostByCategoryReports(UUID branchId, String date, Date comingStartDate, Date comingEndDate) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }
        Branch branch = optionalBranch.get();
        List<Category> categoryList = categoryRepository.findAllByBusiness_Id(branch.getBusiness().getId());
        Map<UUID, Double> productAmount = new HashMap<>();
        for (Category category : categoryList) {
            double amount = 0;
            if (Objects.equals(date, "LAST_DAY")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_CategoryId(Timestamp.valueOf(START_OF_DAY), Timestamp.valueOf(END_OF_DAY), category.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(category.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_CategoryId(Timestamp.valueOf(START_OF_DAY), Timestamp.valueOf(END_OF_DAY), category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getCategory().getId()  , amount);
                    }
                }
            } else if (Objects.equals(date, "LAST_WEEK")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_CategoryId(Timestamp.valueOf(WEEK_START_DAY.atStartOfDay()), Timestamp.valueOf(WEEK_END_DAY.atStartOfDay()), category.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(category.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_CategoryId(Timestamp.valueOf(WEEK_START_DAY.atStartOfDay()), Timestamp.valueOf(WEEK_END_DAY.atStartOfDay()), category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getCategory().getId()  , amount);
                    }
                }
            } else if (Objects.equals(date, "LAST_MONTH")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_CategoryId(Timestamp.valueOf(START_OF_MONTH), Timestamp.valueOf(END_OF_MONTH), category.getId());
                if (!tradeProductList.isEmpty())
                for (TradeProduct product : tradeProductList) {
                    amount += product.getProfit();
                    productAmount.put(category.getId(), amount);
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_CategoryId(Timestamp.valueOf(START_OF_MONTH), Timestamp.valueOf(END_OF_MONTH), category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getCategory().getId()  , amount);
                    }
                }
            } else if (Objects.equals(date, "THIS_MONTH")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_CategoryId(Timestamp.valueOf(THIS_MONTH), currentDay, category.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(category.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_CategoryId(Timestamp.valueOf(THIS_MONTH), currentDay, category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getCategory().getId()  , amount);
                    }
                }
            } else if (Objects.equals(date, "LAST_THIRTY_DAY")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_CategoryId(Timestamp.valueOf(LAST_MONTH), currentDay, category.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(category.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_CategoryId(Timestamp.valueOf(LAST_MONTH), currentDay, category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getCategory().getId()  , amount);
                    }
                }
            } else if (Objects.equals(date, "THIS_YEAR")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_CategoryId(Timestamp.valueOf(START_OF_YEAR_FOR_THIS), currentDay, category.getId());
                if (!tradeProductList.isEmpty())
                for (TradeProduct product : tradeProductList) {
                    amount += product.getProfit();
                    productAmount.put(category.getId(), amount);
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_CategoryId(Timestamp.valueOf(START_OF_YEAR_FOR_THIS), currentDay, category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getCategory().getId()  , amount);
                    }
                }
            } else if (Objects.equals(date, "LAST_YEAR")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_CategoryId(Timestamp.valueOf(START_OF_YEAR), Timestamp.valueOf(END_OF_YEAR), category.getId());
                for (TradeProduct product : tradeProductList) {
                    amount += product.getProfit();
                    productAmount.put(category.getId(), amount);
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_CategoryId(Timestamp.valueOf(START_OF_YEAR), Timestamp.valueOf(END_OF_YEAR), category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getCategory().getId()  , amount);
                    }
                }
            } else if (comingEndDate != null && comingStartDate != null) {
                Timestamp start = new Timestamp(comingStartDate.getTime());
                Timestamp end = new Timestamp(comingEndDate.getTime());
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_CategoryId(start, end, category.getId());
                for (TradeProduct product : tradeProductList) {
                    amount += product.getProfit();
                    productAmount.put(category.getId(), amount);
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_CategoryId(start, end, category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getCategory().getId()  , amount);
                    }
                }
            } else {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByProduct_CategoryId(category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(category.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByProductTypePrice_Product_CategoryId(category.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getCategory().getId(), amount);
                    }
                }
            }
        }
        List<ProfitByCategoryDto> profitByCategoryDtoList = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : productAmount.entrySet()) {
            ProfitByCategoryDto profitByCategoryDto = new ProfitByCategoryDto();
            Optional<Category> optionalCategory = categoryRepository.findById(entry.getKey());
            profitByCategoryDto.setCategoryName(optionalCategory.get().getName());
            profitByCategoryDto.setProfit(entry.getValue());
            profitByCategoryDtoList.add(profitByCategoryDto);
        }
        profitByCategoryDtoList.sort(Comparator.comparing(ProfitByCategoryDto::getProfit).reversed());
        return new ApiResponse("Found", true, profitByCategoryDtoList);
    }

    public ApiResponse benefitAndLostByBrandReports(UUID branchId, String date, Date comingStartDate, Date comingEndDate) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }
        UUID id = optionalBranch.get().getBusiness().getId();
        List<Brand> brandList = brandRepository.findAllByBusiness_Id(id);
        Map<UUID, Double> productAmount = new HashMap<>();
        for (Brand brand : brandList) {
            double amount = 0;
            if (Objects.equals(date, "LAST_DAY")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_BrandId(Timestamp.valueOf(START_OF_DAY), Timestamp.valueOf(END_OF_DAY), brand.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(brand.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_BrandId(Timestamp.valueOf(START_OF_DAY), Timestamp.valueOf(END_OF_DAY), brand.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getBrand().getId(), amount);
                    }
                }
            } else if (Objects.equals(date, "LAST_WEEK")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_BrandId(Timestamp.valueOf(WEEK_START_DAY.atStartOfDay()), Timestamp.valueOf(WEEK_END_DAY.atStartOfDay()), brand.getId());
                if (!tradeProductList.isEmpty())
                for (TradeProduct product : tradeProductList) {
                    amount += product.getProfit();
                    productAmount.put(brand.getId(), amount);
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_BrandId(Timestamp.valueOf(WEEK_START_DAY.atStartOfDay()), Timestamp.valueOf(WEEK_END_DAY.atStartOfDay()), brand.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getBrand().getId(), amount);
                    }
                }
            } else if (Objects.equals(date, "LAST_MONTH")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_BrandId(Timestamp.valueOf(START_OF_MONTH), Timestamp.valueOf(END_OF_MONTH), brand.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(brand.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_BrandId(Timestamp.valueOf(START_OF_MONTH), Timestamp.valueOf(END_OF_MONTH), brand.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getBrand().getId(), amount);
                    }
                }
            } else if (Objects.equals(date, "THIS_MONTH")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_BrandId(Timestamp.valueOf(THIS_MONTH), currentDay, brand.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct tradeProduct : tradeProductList) {
                        for (TradeProduct product : tradeProductList) {
                            amount += product.getProfit();
                            productAmount.put(brand.getId(), amount);
                        }
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_BrandId(Timestamp.valueOf(THIS_MONTH), currentDay, brand.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getBrand().getId(), amount);
                    }
                }
            } else if (Objects.equals(date, "THIS_YEAR")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_BrandId(Timestamp.valueOf(START_OF_YEAR_FOR_THIS), currentDay, brand.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct tradeProduct : tradeProductList) {
                        for (TradeProduct product : tradeProductList) {
                            amount += product.getProfit();
                            productAmount.put(brand.getId(), amount);
                        }
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_BrandId(Timestamp.valueOf(START_OF_YEAR_FOR_THIS), currentDay, brand.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getBrand().getId(), amount);
                    }
                }
            } else if (Objects.equals(date, "LAST_THIRTY_DAY")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_BrandId(Timestamp.valueOf(LAST_MONTH), currentDay, brand.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(brand.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_BrandId(Timestamp.valueOf(LAST_MONTH), currentDay, brand.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getBrand().getId(), amount);
                    }
                }
            } else if (Objects.equals(date, "LAST_YEAR")) {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_BrandId(Timestamp.valueOf(START_OF_YEAR), Timestamp.valueOf(END_OF_YEAR), brand.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(brand.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_BrandId(Timestamp.valueOf(START_OF_YEAR), Timestamp.valueOf(END_OF_YEAR), brand.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getBrand().getId(), amount);
                    }
                }
            } else if (comingEndDate != null && comingStartDate != null) {
                Timestamp start = new Timestamp(comingStartDate.getTime());
                Timestamp end = new Timestamp(comingEndDate.getTime());
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProduct_BrandId(start, end, brand.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(brand.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByCreatedAtBetweenAndProductTypePrice_Product_BrandId(start, end, brand.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getBrand().getId(), amount);
                    }
                }
            } else {
                List<TradeProduct> tradeProductList;
                tradeProductList = tradeProductRepository.findAllByProduct_BrandId(brand.getId());
                if (!tradeProductList.isEmpty()) {
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(brand.getId(), amount);
                    }
                }
                tradeProductList = tradeProductRepository.findAllByProductTypePrice_Product_BrandId(brand.getId());
                if (!tradeProductList.isEmpty()){
                    for (TradeProduct product : tradeProductList) {
                        amount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getProduct().getBrand().getId(), amount);
                    }
                }
            }
        }
        List<ProfitByCategoryDto> profitByCategoryDtoList = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : productAmount.entrySet()) {
            ProfitByCategoryDto profitByCategoryDto = new ProfitByCategoryDto();
            Optional<Brand> optionalBrand = brandRepository.findById(entry.getKey());
            profitByCategoryDto.setCategoryName(optionalBrand.get().getName());
            profitByCategoryDto.setProfit(entry.getValue());
            profitByCategoryDtoList.add(profitByCategoryDto);
        }
        profitByCategoryDtoList.sort(Comparator.comparing(ProfitByCategoryDto::getProfit).reversed());
        return new ApiResponse("Found", true, profitByCategoryDtoList);
    }

    public ApiResponse benefitAndLostByCustomerReports(UUID branchId, String date, Date comingStartDate, Date comingEndDate) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }
        Map<UUID, Double> productAmount = new HashMap<>();
        List<Customer> customerList = customerRepository.findAllByBranchId(branchId);
        for (Customer customer : customerList) {
            double amount = 0;
            if (Objects.equals(date, "LAST_DAY")) {
                List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_CustomerId(Timestamp.valueOf(START_OF_DAY), Timestamp.valueOf(END_OF_DAY), customer.getId());
                if (allByProductId.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                for (TradeProduct product : allByProductId) {

                    amount += product.getProfit();
                    productAmount.put(product.getTrade().getCustomer().getId(), amount);
                }
            } else if (Objects.equals(date, "LAST_WEEK")) {
                List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_CustomerId(Timestamp.valueOf(WEEK_START_DAY.atStartOfDay()), Timestamp.valueOf(WEEK_END_DAY.atStartOfDay()), customer.getId());
                if (allByProductId.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                for (TradeProduct product : allByProductId) {
                    amount += product.getProfit();
                    productAmount.put(product.getProduct().getBrand().getId(), amount);
                }
            } else if (Objects.equals(date, "LAST_MONTH")) {
                List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_CustomerId(Timestamp.valueOf(START_OF_MONTH), Timestamp.valueOf(END_OF_MONTH), customer.getId());
                if (allByProductId.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                for (TradeProduct product : allByProductId) {

                    amount += product.getProfit();
                    productAmount.put(product.getProduct().getBrand().getId(), amount);
                }
            } else if (Objects.equals(date, "THIS_MONTH")) {
                List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_CustomerId(Timestamp.valueOf(THIS_MONTH), currentDay, customer.getId());
                if (allByProductId.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                for (TradeProduct product : allByProductId) {

                    amount += product.getProfit();
                    productAmount.put(product.getTrade().getCustomer().getId(), amount);
                }
            } else if (Objects.equals(date, "THIS_YEAR")) {
                List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_CustomerId(Timestamp.valueOf(START_OF_YEAR_FOR_THIS), currentDay, customer.getId());
                if (allByProductId.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                for (TradeProduct product : allByProductId) {
                    amount += product.getProfit();
                    productAmount.put(product.getTrade().getCustomer().getId(), amount);
                }
            } else if (Objects.equals(date, "LAST_THIRTY_DAY")) {
                List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_CustomerId(Timestamp.valueOf(LAST_MONTH), currentDay, customer.getId());
                if (allByProductId.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                for (TradeProduct product : allByProductId) {
                    amount += product.getProfit();
                    productAmount.put(product.getTrade().getCustomer().getId(), amount);
                }
            } else if (Objects.equals(date, "LAST_YEAR")) {
                List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_CustomerId(Timestamp.valueOf(START_OF_YEAR), Timestamp.valueOf(END_OF_YEAR), customer.getId());
                if (allByProductId.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                for (TradeProduct product : allByProductId) {
                    amount += product.getProfit();
                    productAmount.put(product.getTrade().getCustomer().getId(), amount);
                }
            } else if (comingEndDate != null && comingStartDate != null) {
                Timestamp start = new Timestamp(comingStartDate.getTime());
                Timestamp end = new Timestamp(comingEndDate.getTime());
                List<TradeProduct> allByProductId = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_CustomerId(start, end, customer.getId());
                if (allByProductId.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                for (TradeProduct product : allByProductId) {

                    amount += product.getProfit();
                    productAmount.put(product.getTrade().getCustomer().getId(), amount);
                }
            } else {
                List<TradeProduct> allByProductId = tradeProductRepository.findAllByTrade_CustomerId(customer.getId());
                if (allByProductId.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                for (TradeProduct product : allByProductId) {
                    amount += product.getProfit();
                    productAmount.put(product.getTrade().getCustomer().getId(), amount);
                }
            }
        }
        List<ProfitByCategoryDto> profitByCategoryDtoList = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : productAmount.entrySet()) {
            ProfitByCategoryDto profitByCategoryDto = new ProfitByCategoryDto();
            Optional<Customer> optionalCustomer = customerRepository.findById(entry.getKey());
            profitByCategoryDto.setCategoryName(optionalCustomer.get().getName());
            profitByCategoryDto.setProfit(entry.getValue());
            profitByCategoryDtoList.add(profitByCategoryDto);
        }
        profitByCategoryDtoList.sort(Comparator.comparing(ProfitByCategoryDto::getProfit).reversed());
        return new ApiResponse("Found", true, profitByCategoryDtoList);
    }

    public ApiResponse productionReports(UUID branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("Branch Not Found", false);
        }
        List<Production> productionList = productionRepository.findAllByBranchId(branchId);

        return new ApiResponse("Found", true, productionList);
    }

    public ApiResponse productsReport(UUID customerId, UUID branchId, String date, Date startDate, Date endDate) {

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("not found branch", false);
        }

        if (customerId != null) {
            Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
            if (optionalCustomer.isPresent()) {
                List<ProductReportDto> productReport = getProductReport(customerId, branchId, date, startDate, endDate, true);
                if (productReport != null && productReport.isEmpty()) {
                    return new ApiResponse("Not Found", false);
                }
                return new ApiResponse("all", true, productReport);
            }
        }
        List<ProductReportDto> productReport = getProductReport(customerId, branchId, date, startDate, endDate, false);
        if (productReport != null && productReport.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }
        return new ApiResponse("all", true, productReport);
    }

    private List<ProductReportDto> getProductReport(UUID customerId, UUID branchId, String date, Date startDate, Date endDate, boolean isByCustomerId) {
        Map<UUID, Double> productAmount = new HashMap<>();
        List<ProductReportDto> all = new ArrayList<>();
        Timestamp startTimestamp = null;
        Timestamp endTimestamp = null;

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);



        if (startDate != null && endDate != null) {
            startTimestamp = new Timestamp(startDate.getTime());
            endTimestamp = new Timestamp(endDate.getTime());
        }

        switch (date) {
            case ("LAST_DAY"):
                startTimestamp = Timestamp.valueOf(START_OF_DAY);
                endTimestamp = Timestamp.valueOf(END_OF_DAY);
                break;
            case ("LAST_WEEK"):
                startTimestamp = Timestamp.valueOf(WEEK_START_DAY.atStartOfDay());
                endTimestamp = Timestamp.valueOf(WEEK_END_DAY.atStartOfDay());
                break;
            case ("LAST_THIRTY_DAY"):
                startTimestamp = Timestamp.valueOf(END_OF_MONTH);
                endTimestamp = currentDay;
                break;
            case ("THIS_YEAR"):
                startTimestamp = Timestamp.valueOf(START_OF_YEAR_FOR_THIS);
                endTimestamp = currentDay;
                break;
            case ("LAST_YEAR"):
                startTimestamp = Timestamp.valueOf(START_OF_YEAR);
                endTimestamp = Timestamp.valueOf(END_OF_MONTH);
                break;
            case ("LAST_MONTH"):
                startTimestamp = Timestamp.valueOf(START_OF_MONTH);
                endTimestamp = Timestamp.valueOf(END_OF_MONTH);
                break;
            case ("THIS_MONTH"):
                startTimestamp = Timestamp.valueOf(THIS_MONTH);
                endTimestamp = currentDay;
                break;
            case ("ALL"):
                List<Trade> allByCustomerId = tradeRepository.findAllByCustomer_Id(customerId);
                for (Trade trade : allByCustomerId) {
                    List<TradeProduct> allTradeCustomerId = tradeProductRepository.findAllByTradeId(trade.getId());
                    for (TradeProduct tradeProduct : allTradeCustomerId) {
                        if (tradeProduct.getProduct() != null) {
                            List<TradeProduct> allByProductId = tradeProductRepository.findAllByProduct_IdAndTrade_CustomerId(tradeProduct.getProduct().getId(), customerId);
                            double totalAmount = 0;
                            for (TradeProduct product : allByProductId) {
                                totalAmount += product.getProfit();
                                productAmount.put(product.getProduct().getId(), totalAmount);
                            }
                        }
                    }
                    for (TradeProduct tradeProduct : allTradeCustomerId) {
                        if (tradeProduct.getProductTypePrice() != null) {
                            List<TradeProduct> allByProductTypePriceId = tradeProductRepository.findAllByProductTypePriceIdAndTrade_CustomerId(tradeProduct.getProductTypePrice().getId(), customerId);
                            double totalAmount = 0;
                            for (TradeProduct product : allByProductTypePriceId) {
                                totalAmount += product.getProfit();
                                productAmount.put(product.getProductTypePrice().getId(), totalAmount);
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
        if (isByCustomerId && !date.equals("ALL")) {
            List<Trade> allByCustomerId = tradeRepository.findAllByCreatedAtBetweenAndCustomer_Id(startTimestamp, endTimestamp, customerId);
            for (Trade trade : allByCustomerId) {
                List<TradeProduct> allTradeCustomerId = tradeProductRepository.findAllByTradeId(trade.getId());
                for (TradeProduct tradeProduct : allTradeCustomerId) {
                    if (tradeProduct.getProduct() != null) {
                        List<TradeProduct> allByProductId = tradeProductRepository.findAllByProduct_IdAndTrade_CustomerId(tradeProduct.getProduct().getId(), customerId);
                        double totalAmount = 0;
                        for (TradeProduct product : allByProductId) {
                            totalAmount += product.getProfit();
                            productAmount.put(product.getProduct().getId(), totalAmount);
                        }
                    }
                }
                for (TradeProduct tradeProduct : allTradeCustomerId) {
                    if (tradeProduct.getProductTypePrice() != null) {
                        List<TradeProduct> allByProductTypePriceId = tradeProductRepository.findAllByProductTypePriceIdAndTrade_CustomerId(tradeProduct.getProductTypePrice().getId(), customerId);
                        double totalAmount = 0;
                        for (TradeProduct product : allByProductTypePriceId) {
                            totalAmount += product.getProfit();
                            productAmount.put(product.getProductTypePrice().getId(), totalAmount);
                        }
                    }
                }
            }
        } else if (!date.equals("ALL")) {
            if (optionalBranch.isEmpty()) {
                return null;
            }
            List<TradeProduct> allTradeBranch = tradeProductRepository.findAllByCreatedAtBetweenAndTrade_BranchId(startTimestamp, endTimestamp, branchId);
            for (TradeProduct tradeProduct : allTradeBranch) {
                if (tradeProduct.getProduct() != null) {
                    List<TradeProduct> allByProductId = tradeProductRepository.findAllByProduct_Id(tradeProduct.getProduct().getId());
                    double totalAmount = 0;
                    for (TradeProduct product : allByProductId) {
                        totalAmount += product.getProfit();
                        productAmount.put(product.getProduct().getId(), totalAmount);
                    }
                }
            }
            for (TradeProduct tradeProduct : allTradeBranch) {
                if (tradeProduct.getProductTypePrice() != null) {
                    List<TradeProduct> allByProductTypePriceId = tradeProductRepository.findAllByProductTypePriceId(tradeProduct.getProductTypePrice().getId());
                    double totalAmount = 0;
                    for (TradeProduct product : allByProductTypePriceId) {
                        totalAmount += product.getProfit();
                        productAmount.put(product.getProductTypePrice().getId(), totalAmount);
                    }
                }
            }
        }

        for (Map.Entry<UUID, Double> productAmounts : productAmount.entrySet()) {
            Optional<Product> optionalProduct = productRepository.findById(productAmounts.getKey());
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                ProductReportDto productReportDto = new ProductReportDto();
                productReportDto.setName(product.getName());
                productReportDto.setBranch(product.getBranch().get(0).getName());
                productReportDto.setBarcode(product.getBarcode());
                productReportDto.setSalePrice(product.getSalePrice());
                productReportDto.setBuyPrice(product.getBuyPrice());
                productReportDto.setAmount(productAmounts.getValue());
                all.add(productReportDto);
            }else {
                Optional<ProductTypePrice> productTypePrice = productTypePriceRepository.findById(productAmounts.getKey());
                if (productTypePrice.isPresent()) {
                    ProductReportDto productReportDto = new ProductReportDto();
                    productReportDto.setName(productTypePrice.get().getName());
                    productReportDto.setBranch(optionalBranch.get().getName());
                    productReportDto.setBarcode(productTypePrice.get().getBarcode());
                    productReportDto.setSalePrice(productTypePrice.get().getSalePrice());
                    productReportDto.setBuyPrice(productTypePrice.get().getBuyPrice());
                    productReportDto.setAmount(productAmounts.getValue());
                    all.add(productReportDto);
                }
            }
        }
        return all;
    }
}
