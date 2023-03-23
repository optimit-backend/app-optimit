package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Customer;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.mapper.PaymentMapper;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TradeService {
    @Autowired
    TradeRepository tradeRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductTypePriceRepository productTypePriceRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    PaymentStatusRepository paymentStatusRepository;

    @Autowired
    PayMethodRepository payMethodRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TradeProductRepository tradeProductRepository;

    @Autowired
    TradeHistoryRepository tradeHistoryRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CurrentCourceRepository currentCourceRepository;

    @Autowired
    CurrencyService currencyService;

    @Autowired
    WarehouseService warehouseService;

    @Autowired
    FifoCalculationService fifoCalculationService;

    @Autowired
    WarehouseRepository warehouseRepository;

    private final PaymentMapper paymentMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final ProductTypeComboRepository productTypeComboRepository;
    private final PaymentRepository paymentRepository;

    @SneakyThrows
    public ApiResponse create(TradeDTO tradeDTO) {
        UUID branchId = tradeDTO.getBranchId();
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("not found branch", false);
        }
        Branch branch = optionalBranch.get();
        Business business = branch.getBusiness();

        List<Trade> allTrade = tradeRepository.findAllByBranch_BusinessId(business.getId());
        int size = allTrade.size();


        Optional<Subscription> optionalSubscription = subscriptionRepository.findByBusinessIdAndActiveTrue(business.getId());
        if (optionalSubscription.isEmpty()) {
            return new ApiResponse("tariff aktiv emas", false);
        }

        Subscription subscription = optionalSubscription.get();

        if (subscription.getTariff().getTradeAmount() >= size || subscription.getTariff().getTradeAmount() == 0) {
            Trade trade = new Trade();
            return createOrEditTrade(trade, tradeDTO);
        }
        return new ApiResponse("You have opened a sufficient branch according to the trade", false);
    }

    public ApiResponse edit(UUID id, TradeDTO tradeDTO) {
        Optional<Trade> optionalTrade = tradeRepository.findById(id);
        if (optionalTrade.isEmpty()) {
            return new ApiResponse("NOT FOUND TRADE", false);
        }
        Trade trade = optionalTrade.get();
        if (!trade.isEditable()) return new ApiResponse("YOU CAN NOT EDIT AFTER 24 HOUR", false);
        Timestamp createdAt = trade.getCreatedAt();
        long difference = System.currentTimeMillis() - createdAt.getTime();
        long oneDay = 1000 * 60 * 60 * 24;
        if (difference > oneDay) {
            trade.setEditable(false);
            return new ApiResponse("YOU CAN NOT EDIT AFTER 24 HOUR", false);
        }
        return createOrEditTrade(trade, tradeDTO);
    }

    public ApiResponse createOrEditTrade(Trade trade, TradeDTO tradeDTO) {

        Optional<User> optionalUser = userRepository.findById(tradeDTO.getUserId());
        if (optionalUser.isEmpty()) {
            return new ApiResponse("TRADER NOT FOUND", false);
        }
        trade.setTrader(optionalUser.get());

        Optional<Branch> optionalBranch = branchRepository.findById(tradeDTO.getBranchId());
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("BRANCH NOT FOUND", false);
        }
        Branch branch = optionalBranch.get();
        trade.setBranch(branch);

        Optional<PaymentStatus> optionalPaymentStatus = paymentStatusRepository.findById(tradeDTO.getPaymentStatusId());
        if (optionalPaymentStatus.isEmpty()) {
            return new ApiResponse("PAYMENTSTATUS NOT FOUND", false);
        }
        trade.setPaymentStatus(optionalPaymentStatus.get());

        if (tradeDTO.getPaymentDtoList().isEmpty()) {
            return new ApiResponse("PAYMENT METHOD NOT FOUND", false);
        }

        if (tradeDTO.getProductTraderDto().isEmpty()) {
            return new ApiResponse("PRODUCT LIST NOT FOUND", false);
        }

        List<TradeProductDto> tradeProductDtoList = tradeDTO.getProductTraderDto();
        if (!branch.getBusiness().getSaleMinus()) {
            HashMap<UUID, Double> map = new HashMap<>();
            for (TradeProductDto dto : tradeProductDtoList) {
                double tradedQuantity = dto.getTradedQuantity();
                if (dto.getTradeProductId() != null){
                    Optional<TradeProduct> optionalTradeProduct = tradeProductRepository.findById(dto.getTradeProductId());
                    if (optionalTradeProduct.isPresent()){
                        tradedQuantity -= optionalTradeProduct.get().getTradedQuantity();
                        if (tradedQuantity < 0) tradedQuantity = 0d;
                    }
                }
                if (dto.getType().equalsIgnoreCase("single")) {
                    UUID productId = dto.getProductId();
                    if (!productRepository.existsById(productId)) return new ApiResponse("PRODUCT NOT FOUND", false);
                    map.put(productId, map.getOrDefault(productId, 0d) + tradedQuantity);
                } else if (dto.getType().equalsIgnoreCase("many")) {
                    UUID productId = dto.getProductTypePriceId();
                    if (!productTypePriceRepository.existsById(productId))
                        return new ApiResponse("PRODUCT NOT FOUND", false);
                    map.put(productId, map.getOrDefault(productId, 0d) + tradedQuantity);
                } else if (dto.getType().equalsIgnoreCase("combo")) {
                    UUID productId = dto.getProductId();
                    List<ProductTypeCombo> comboList = productTypeComboRepository.findAllByMainProductId(productId);
                    if (comboList.isEmpty()) return new ApiResponse("PRODUCT NOT FOUND", false);
                    for (ProductTypeCombo combo : comboList) {
                        UUID contentProduct = combo.getContentProduct().getId();
                        map.put(contentProduct, map.getOrDefault(contentProduct, 0d) + tradedQuantity * combo.getAmount());
                    }
                } else {
                    return new ApiResponse("PRODUCT NOT FOUND", false);
                }
            }

            if (!warehouseService.checkBeforeTrade(branch, map)) return new ApiResponse("NOT ENOUGH PRODUCT", false);
        }

        double debtSum = trade.getDebtSum();
        if (tradeDTO.getDebtSum() > 0 || debtSum != tradeDTO.getDebtSum()) {
            if (tradeDTO.getCustomerId() == null) return new ApiResponse("CUSTOMER NOT FOUND", false);
            Optional<Customer> optionalCustomer = customerRepository.findById(tradeDTO.getCustomerId());
            if (optionalCustomer.isEmpty()) return new ApiResponse("CUSTOMER NOT FOUND", false);
            Customer customer = optionalCustomer.get();
            trade.setCustomer(customer);
            customer.setDebt(customer.getDebt() - debtSum + tradeDTO.getDebtSum());
            customer.setPayDate(tradeDTO.getPayDate());
            customerRepository.save(customer);
        } else if (tradeDTO.getCustomerId() != null){
            Optional<Customer> optionalCustomer = customerRepository.findById(tradeDTO.getCustomerId());
            if (optionalCustomer.isEmpty()) return new ApiResponse("CUSTOMER NOT FOUND", false);
            Customer customer = optionalCustomer.get();
            trade.setCustomer(customer);
        }

        trade.setPayDate(tradeDTO.getPayDate());
        trade.setTotalSum(tradeDTO.getTotalSum());
        trade.setPaidSum(tradeDTO.getPaidSum());
        trade.setDebtSum(tradeDTO.getDebtSum());
        tradeRepository.save(trade);

        if(paymentRepository.existsByTradeId(trade.getId())){
            List<Payment> paymentList = paymentRepository.findAllByTradeId(trade.getId());
            if (!paymentList.isEmpty()){
                for (Payment payment : paymentList) {
                    paymentRepository.deleteById(payment.getId());
                }
            }
//            paymentRepository.deleteAllByTradeId(trade.getId());
        }

        List<Payment> paymentList = new ArrayList<>();
        for (PaymentDto paymentDto : tradeDTO.getPaymentDtoList()) {
            Optional<PaymentMethod> optionalPaymentMethod = payMethodRepository.findById(paymentDto.getPaymentMethodId());
            if (optionalPaymentMethod.isEmpty()) return new ApiResponse("PAYMENT METHOD NOT FOUND", false);
            paymentList.add(new Payment(
                    trade,
                    optionalPaymentMethod.get(),
                    paymentDto.getPaidSum()
            ));
        }

        if (paymentList.isEmpty()) {
            return new ApiResponse("PAYMENT METHOD NOT FOUND", false);
        }
        paymentRepository.saveAll(paymentList);
        trade.setPayMethod(paymentList.get(0).getPayMethod());

        /**
         * SOTILGAN PRODUCT SAQLANDI YANI TRADERPRODUCT
         */
        List<TradeProduct> tradeProductList = new ArrayList<>();

        double profit = 0;

        for (TradeProductDto tradeProductDto : tradeProductDtoList) {
            if (tradeProductDto.isDelete()) {
                if (tradeProductRepository.existsById(tradeProductDto.getTradeProductId())) {
                    TradeProduct tradeProduct = tradeProductRepository.getById(tradeProductDto.getTradeProductId());
                    double tradedQuantity = tradeProductDto.getTradedQuantity(); // to send fifo calculation
                    tradeProductDto.setTradedQuantity(0);//  to make sold quantity 0
//                    profit -= tradeProduct.getProfit();// to subtract profit of product
                    TradeProduct savedTradeProduct = warehouseService.createOrEditTrade(tradeProduct.getTrade().getBranch(), tradeProduct, tradeProductDto);
                    fifoCalculationService.returnedTrade(branch, savedTradeProduct, tradedQuantity);
                    tradeProductRepository.deleteById(tradeProductDto.getTradeProductId());
                }
            } else if (tradeProductDto.getTradeProductId() == null) {
                TradeProduct tradeProduct = warehouseService.createOrEditTrade(branch, new TradeProduct(), tradeProductDto);
                if (tradeProduct != null) {
                    tradeProduct.setTrade(trade);
                    TradeProduct savedTradeProduct = fifoCalculationService.createOrEditTradeProduct(branch, tradeProduct, tradeProduct.getTradedQuantity());
                    tradeProductList.add(savedTradeProduct);
                    profit += savedTradeProduct.getProfit();
                }
            } else {
                Optional<TradeProduct> optionalTradeProduct = tradeProductRepository.findById(tradeProductDto.getTradeProductId());
                if (optionalTradeProduct.isEmpty()) continue;
                TradeProduct tradeProduct = optionalTradeProduct.get();
                if (tradeProduct.getTradedQuantity() == tradeProductDto.getTradedQuantity()) {
                    profit +=tradeProduct.getProfit();
                    continue;
                }
                double difference = tradeProductDto.getTradedQuantity() - tradeProduct.getTradedQuantity();
                tradeProduct = warehouseService.createOrEditTrade(branch, tradeProduct, tradeProductDto);
                if (tradeProduct != null) {
                    if (difference > 0) {
                        fifoCalculationService.createOrEditTradeProduct(branch, tradeProduct, difference);
                    } else if (difference < 0) {
                        fifoCalculationService.returnedTrade(branch, tradeProduct, -difference);
                    }
                    tradeProductList.add(tradeProduct);
                    profit += tradeProduct.getProfit();
                }
            }
        }
        trade.setTotalProfit(profit);
        tradeRepository.save(trade);
        tradeProductRepository.saveAll(tradeProductList);
        return new ApiResponse("SAVED!", true);
    }

    public ApiResponse getOne(UUID id) {
        Optional<Trade> optionalTrade = tradeRepository.findById(id);
        if (optionalTrade.isEmpty()) return new ApiResponse("NOT FOUND", false);
        Trade trade = optionalTrade.get();
        List<TradeProduct> allByTradeId = tradeProductRepository.findAllByTradeId(trade.getId());
        if (allByTradeId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        for (TradeProduct tradeProduct : allByTradeId) {
            Optional<Warehouse> optionalWarehouse = null;
            if (tradeProduct.getProduct() != null)
                optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(trade.getBranch().getId(), tradeProduct.getProduct().getId());
            else
                optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(trade.getBranch().getId(), tradeProduct.getProductTypePrice().getId());

            tradeProduct.setRemainQuantity(optionalWarehouse.map(Warehouse::getAmount).orElse(0d));
        }
        List<Payment> paymentList = paymentRepository.findAllByTradeId(trade.getId());
        List<PaymentDto> paymentDtoList = paymentMapper.toDtoList(paymentList);
        TradeGetOneDto tradeGetOneDto = new TradeGetOneDto();
        tradeGetOneDto.setTrade(trade);
        tradeGetOneDto.setTradeProductList(allByTradeId);
        tradeGetOneDto.setPaymentDtoList(paymentDtoList);
        return new ApiResponse(true, tradeGetOneDto);
    }

    public ApiResponse deleteTrade(UUID id) {
        Optional<Trade> byId = tradeRepository.findById(id);
        if (byId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        tradeRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse deleteByTraderId(UUID trader_id) {
        if (!tradeRepository.existsByTraderId(trader_id)) return new ApiResponse("TRADER NOT FOUND", false);
        tradeRepository.deleteByTrader_Id(trader_id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse deleteAllByTraderId(UUID trader_id) {
        if (!tradeRepository.existsByTraderId(trader_id)) return new ApiResponse("TRADER NOT FOUND", false);
        tradeRepository.deleteAllByTrader_Id(trader_id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getAllByTraderId(UUID trader_id) {
        List<Trade> allByTrader_id = tradeRepository.findAllByTrader_Id(trader_id);
        if (allByTrader_id.isEmpty()) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, allByTrader_id);
    }

    public ApiResponse getByCustomerId(UUID customer_id) {
        List<Trade> allByCustomer_id = tradeRepository.findAllByCustomer_Id(customer_id);
        if (allByCustomer_id.isEmpty()) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, allByCustomer_id);
    }

    public ApiResponse getByPayDate(Timestamp payDate) throws ParseException {
        List<Trade> allByPayDate = tradeRepository.findTradeByOneDate(payDate);
        if (allByPayDate.isEmpty()) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, allByPayDate);
    }

    public ApiResponse getByPayStatus(UUID paymentStatus_id) {
        List<Trade> allByPaymentStatus_id = tradeRepository.findAllByPaymentStatus_Id(paymentStatus_id);
        if (allByPaymentStatus_id.isEmpty()) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, allByPaymentStatus_id);
    }

    public ApiResponse getByPayMethod(UUID payMethod_id) {
        List<Trade> allByPaymentMethod_id = new ArrayList<>();
        List<Payment> paymentList = paymentRepository.findAllByPayMethodId(payMethod_id);
        for (Payment payment : paymentList) {
            allByPaymentMethod_id.add(payment.getTrade());
        }
        if (allByPaymentMethod_id.isEmpty()) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, allByPaymentMethod_id);
    }

    public ApiResponse getByAddress(UUID address_id) {
        List<Trade> allByAddress_id = tradeRepository.findAllByAddress_Id(address_id);
        if (allByAddress_id.isEmpty()) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, allByAddress_id);
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        List<Trade> allByBusinessId = tradeRepository.findAllByBranch_BusinessId(businessId);
        if (allByBusinessId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        /*List<Trade> tradeList = new ArrayList<>();
        for (Trade trade : allByBusinessId) {
            Trade trade1 = generateTradeByActiveCourse(trade);
            tradeList.add(trade1);
        }*/
        return new ApiResponse("FOUND", true, allByBusinessId);
    }

    public ApiResponse createPdf(UUID id, HttpServletResponse response) throws IOException {

        Optional<Trade> tradeOptional = tradeRepository.findById(id);
        PDFService pdfService = new PDFService();

        pdfService.createPdf(tradeOptional.get(), response);

        return new ApiResponse("CREATED", true);
    }

//    private Trade generateTradeByActiveCourse(Trade trade){
//        UUID busnessId = trade.getBranch().getBusiness().getId();
//        double avans = currencyService.getValueByActiveCourse(trade.getPaidSum(), busnessId);
//        trade.setPaidSum(avans);
//        double totalSum = currencyService.getValueByActiveCourse(trade.getTotalSum(), busnessId);
//        trade.setTotalSum(totalSum);
////        sac
//
//
//        for (TradeProduct tradeProduct : tradeProductRepository.findAllByTradeId(trade.getId())) {
//            double salePrice = currencyService.getValueByActiveCourse(tradeProduct.getTotalSalePrice(), busnessId);
//            tradeProduct.setTotalSalePrice(salePrice);
//            Product product = tradeProduct.getProduct();
//            product.setSalePrice(salePrice);
//            double buyPrice = currencyService.getValueByActiveCourse(product.getBuyPrice(), busnessId);
//            product.setBuyPrice(buyPrice);
//        }
//        return trade;
//    }
}
