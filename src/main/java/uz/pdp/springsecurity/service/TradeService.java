package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Currency;
import uz.pdp.springsecurity.entity.Customer;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.SalaryStatus;
import uz.pdp.springsecurity.mapper.PaymentMapper;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;

    private final CustomerRepository customerRepository;

    private final BranchRepository branchRepository;

    private final PaymentStatusRepository paymentStatusRepository;

    private final PayMethodRepository payMethodRepository;

    private final UserRepository userRepository;

    private final TradeProductRepository tradeProductRepository;

    private final CurrencyRepository currencyRepository;

    private final WarehouseService warehouseService;

    private final FifoCalculationService fifoCalculationService;

    private final WarehouseRepository warehouseRepository;

    private final PaymentMapper paymentMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final ProductTypeComboRepository productTypeComboRepository;
    private final PaymentRepository paymentRepository;
    private final SalaryCountService salaryCountService;
    private final AgreementRepository agreementRepository;
    private final BalanceService balanceService;

    @SneakyThrows
    public ApiResponse create(TradeDTO tradeDTO) {
        Optional<Branch> optionalBranch = branchRepository.findById(tradeDTO.getBranchId());
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("not found branch", false);
        }
        UUID businessId = optionalBranch.get().getBusiness().getId();

        if (!subscriptionRepository.existsByBusinessIdAndActiveTrue(businessId)) {
            return new ApiResponse("NOT FOUND ACTIVE TARIFF", false);
        }

        Trade trade = new Trade();
        trade.setBranch(optionalBranch.get());
        trade.setLid(tradeDTO.isLid());
        return createOrEditTrade(trade, tradeDTO);
    }

    public ApiResponse edit(UUID id, TradeDTO tradeDTO) {
        Optional<Trade> optionalTrade = tradeRepository.findById(id);
        if (optionalTrade.isEmpty()) {
            return new ApiResponse("NOT FOUND TRADE", false);
        }
        Trade trade = optionalTrade.get();
        if (!trade.isEditable()) return new ApiResponse("YOU CAN NOT EDIT AFTER 7 DAYS", false);
        int days = LocalDateTime.now().getDayOfYear() - trade.getCreatedAt().toLocalDateTime().getDayOfYear();
        if (days > 6) {
            trade.setEditable(false);
            return new ApiResponse("YOU CAN NOT EDIT AFTER 7 DAYS", false);
        }
        return createOrEditTrade(trade, tradeDTO);
    }

    @Transactional
    public ApiResponse createOrEditTrade(Trade trade, TradeDTO tradeDTO) {
        Branch branch = trade.getBranch();

        Optional<User> optionalUser = userRepository.findById(tradeDTO.getUserId());
        if (optionalUser.isEmpty()) {
            return new ApiResponse("TRADER NOT FOUND", false);
        }
        trade.setTrader(optionalUser.get());

        Optional<PaymentStatus> optionalPaymentStatus = paymentStatusRepository.findById(tradeDTO.getPaymentStatusId());
        if (optionalPaymentStatus.isEmpty()) {
            return new ApiResponse("PAYMENT STATUS NOT FOUND", false);
        }
        trade.setPaymentStatus(optionalPaymentStatus.get());

        Optional<Agreement> optionalAgreementKpi = agreementRepository.findByUserIdAndSalaryStatus(trade.getTrader().getId(), SalaryStatus.KPI);
        if (optionalAgreementKpi.isEmpty()) {
            return new ApiResponse("AGREEMENT NOT FOUND", false);
        }

        if (tradeDTO.getPaymentDtoList().isEmpty()) {
            return new ApiResponse("PAYMENT METHOD NOT FOUND", false);
        }

        if (tradeDTO.getProductTraderDto().isEmpty()) {
            return new ApiResponse("PRODUCT LIST NOT FOUND", false);
        }

        if (!branch.getBusiness().isSaleMinus()) {
            HashMap<UUID, Double> map = new HashMap<>();
            for (TradeProductDto dto : tradeDTO.getProductTraderDto()) {
                double tradedQuantity = dto.getTradedQuantity();
                if (dto.getTradeProductId() != null) {
                    Optional<TradeProduct> optionalTradeProduct = tradeProductRepository.findById(dto.getTradeProductId());
                    if (optionalTradeProduct.isPresent()) {
                        tradedQuantity -= optionalTradeProduct.get().getTradedQuantity();
                        if (tradedQuantity < 0) tradedQuantity = 0d;
                    }
                }
                if (dto.getType().equalsIgnoreCase("single")) {
                    UUID productId = dto.getProductId();
//                    if (!productRepository.existsById(productId)) return new ApiResponse("PRODUCT NOT FOUND", false);
                    map.put(productId, map.getOrDefault(productId, 0d) + tradedQuantity);
                } else if (dto.getType().equalsIgnoreCase("many")) {
                    UUID productId = dto.getProductTypePriceId();
//                    if (!productTypePriceRepository.existsById(productId)) return new ApiResponse("PRODUCT NOT FOUND", false);
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
                    return new ApiResponse("PRODUCT TYPE NOT FOUND", false);
                }
            }

            if (!warehouseService.checkBeforeTrade(branch, map)) return new ApiResponse("NOT ENOUGH PRODUCT", false);
        }
        double unFrontPayment = 0;
        double debtSum = trade.getDebtSum();
        if (tradeDTO.getDebtSum() > 0 || debtSum != tradeDTO.getDebtSum()) {
            if (tradeDTO.getCustomerId() == null) return new ApiResponse("CUSTOMER NOT FOUND", false);
            Optional<Customer> optionalCustomer = customerRepository.findById(tradeDTO.getCustomerId());
            if (optionalCustomer.isEmpty()) return new ApiResponse("CUSTOMER NOT FOUND", false);
            double newDebt = tradeDTO.getDebtSum() - debtSum;
            Customer customer = optionalCustomer.get();
            double debt = -customer.getDebt() + trade.getPaidSum();
            if (customer.getDebt() < 0 && newDebt > 0) {
                unFrontPayment = Math.min(debt, newDebt);
            }
            trade.setCustomer(customer);
            customer.setDebt(newDebt - debt);
            customer.setPayDate(tradeDTO.getPayDate());
            customerRepository.save(customer);
        } else if (tradeDTO.getCustomerId() != null) {
            Optional<Customer> optionalCustomer = customerRepository.findById(tradeDTO.getCustomerId());
            if (optionalCustomer.isEmpty()) return new ApiResponse("CUSTOMER NOT FOUND", false);
            Customer customer = optionalCustomer.get();
            trade.setCustomer(customer);
        }


        trade.setDollar(tradeDTO.getDollar());
        trade.setGross(tradeDTO.getGross());
        trade.setPayDate(tradeDTO.getPayDate());
        trade.setTotalSum(tradeDTO.getTotalSum());
        trade.setPaidSum(tradeDTO.getPaidSum() + unFrontPayment);
        trade.setDebtSum(tradeDTO.getDebtSum() - unFrontPayment);
        Optional<Currency> optionalCurrency = currencyRepository.findByBusinessId(branch.getBusiness().getId());
        if (optionalCurrency.isPresent()) {
            trade.setDebtSumDollar(Math.round(trade.getDebtSum()/optionalCurrency.get().getCourse()*100)/100.);
            trade.setPaidSumDollar(Math.round(trade.getPaidSum()/optionalCurrency.get().getCourse()*100)/100.);
            trade.setTotalSumDollar(Math.round(trade.getTotalSum()/optionalCurrency.get().getCourse()*100)/100.);
        }
        tradeRepository.save(trade);

        if (paymentRepository.existsByTradeId(trade.getId())) {
            List<Payment> paymentList = paymentRepository.findAllByTradeId(trade.getId());
            if (!paymentList.isEmpty()) {
                for (Payment payment : paymentList) {
                    paymentRepository.deleteById(payment.getId());
                }
            }
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
        paymentList.get(0).setPaidSum(paymentList.get(0).getPaidSum() + unFrontPayment);
        paymentRepository.saveAll(paymentList);
        trade.setPayMethod(paymentList.get(0).getPayMethod());

        List<TradeProduct> tradeProductList = new ArrayList<>();

        double profit = 0;

        for (TradeProductDto tradeProductDto : tradeDTO.getProductTraderDto()) {
            if (tradeProductDto.isDelete()) {
                Optional<TradeProduct> optionalTradeProduct = tradeProductRepository.findById(tradeProductDto.getTradeProductId());
                if (optionalTradeProduct.isPresent()) {
                    TradeProduct tradeProduct = optionalTradeProduct.get();
                    double tradedQuantity = tradeProductDto.getTradedQuantity(); // to send fifo calculation
                    tradeProductDto.setTradedQuantity(0);//  to make sold quantity 0
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
                    profit += tradeProduct.getProfit();
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
        countKPI(optionalAgreementKpi.get(), trade, tradeProductList);
        tradeRepository.save(trade);
        tradeProductRepository.saveAll(tradeProductList);

        balanceService.edit(branch.getId(), true, tradeDTO.getPaymentDtoList());
        return new ApiResponse("SAVED!", true);
    }

    private void countKPI(Agreement agreementKpi, Trade trade, List<TradeProduct> tradeProductList) {
        double kpiProduct = countKPIProduct(tradeProductList);
        if (agreementKpi.getPrice() > 0 || kpiProduct > 0) {
            Double kpiD = trade.getKpi();
            double kpi = kpiD == null ? 0 : kpiD;
            double salarySum = trade.getTotalSum() * agreementKpi.getPrice() / 100;
            salaryCountService.add(new SalaryCountDto(
                    1,
                    salarySum + kpiProduct - kpi,
                    agreementKpi.getId(),
                    trade.getBranch().getId(),
                    new Date(),
                    "Savdo ulushi"
            ));
            trade.setKpi(salarySum + kpiProduct);
        }
    }

    private double countKPIProduct(List<TradeProduct> tradeProductList){
        double kpi = 0;
        for (TradeProduct tradeProduct : tradeProductList) {
            if (tradeProduct.getProduct() != null) {
                kpi += countKPIProductHelper(tradeProduct.getProduct(), tradeProduct.getTradedQuantity(), tradeProduct.getTotalSalePrice());
            }
            else {
                kpi += countKPIProductHelper(tradeProduct.getProductTypePrice().getProduct(), tradeProduct.getTradedQuantity(), tradeProduct.getTotalSalePrice());
            }
        }
        return kpi;
    }

    private double countKPIProductHelper(Product product, double quantity, double totalPrice){
        if (product.getKpi() != null & product.getKpiPercent() != null) {
            if (product.getKpiPercent()){
                return totalPrice * product.getKpi() / 100;
            }else {
                return product.getKpi() * quantity;
            }
        }
        return 0;
    }

    public ApiResponse getOne(UUID id) {
        Optional<Trade> optionalTrade = tradeRepository.findById(id);
        if (optionalTrade.isEmpty()) return new ApiResponse("NOT FOUND", false);
        Trade trade = optionalTrade.get();
        List<TradeProduct> tradeProductList = tradeProductRepository.findAllByTradeId(trade.getId());
        if (tradeProductList.isEmpty()) return new ApiResponse("NOT FOUND", false);
        for (TradeProduct tradeProduct : tradeProductList) {
            Optional<Warehouse> optionalWarehouse;
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
        tradeGetOneDto.setTradeProductList(tradeProductList);
        tradeGetOneDto.setPaymentDtoList(paymentDtoList);
        return new ApiResponse(true, tradeGetOneDto);
    }

    public ApiResponse delete(UUID tradeId) {
        Optional<Trade> optionalTrade = tradeRepository.findById(tradeId);
        if (optionalTrade.isEmpty()) return new ApiResponse("NOT FOUND", false);
        Trade trade = optionalTrade.get();
        if (!trade.isEditable()) return new ApiResponse("YOU CAN NOT DELETE AFTER 30 DAYS", false);
        int days = LocalDateTime.now().getDayOfYear() - trade.getCreatedAt().toLocalDateTime().getDayOfYear();
        if (days > 30) {
            trade.setEditable(false);
            return new ApiResponse("YOU CAN NOT DELETE AFTER 30 DAYS", false);
        }

        for (TradeProduct tradeProduct : tradeProductRepository.findAllByTradeId(tradeId)) {
            double amount = warehouseService.createOrEditWareHouseHelper(trade.getBranch(), tradeProduct.getProduct(), tradeProduct.getProductTypePrice(), tradeProduct.getTradedQuantity());
            if (amount < tradeProduct.getTradedQuantity())
                fifoCalculationService.returnedTrade(trade.getBranch(), tradeProduct, tradeProduct.getTradedQuantity() - amount);
        }

        if (trade.getCustomer() != null) {
            Customer customer = trade.getCustomer();
            customer.setDebt(customer.getDebt() - (trade.getPaidSum() + trade.getDebtSum()));
        }

        if (trade.getKpi() != null){
            Optional<Agreement> optionalAgreementKpi = agreementRepository.findByUserIdAndSalaryStatus(trade.getTrader().getId(), SalaryStatus.KPI);
            if (optionalAgreementKpi.isPresent()) {
                double kpi = trade.getKpi();
                salaryCountService.add(new SalaryCountDto(
                        1,
                        -kpi,
                        optionalAgreementKpi.get().getId(),
                        trade.getBranch().getId(),
                        new Date(),
                        "deleted trade"
                ));
            }
        }


        for (Payment payment : paymentRepository.findAllByTradeId(tradeId)) {
            balanceService.edit(trade.getBranch().getId(), payment.getPaidSum(), Boolean.FALSE, payment.getPayMethod().getId());
        }

        tradeRepository.deleteById(tradeId);
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

    public ApiResponse getByPayDate(Timestamp payDate) {
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
        List<Trade> allByBusinessId = tradeRepository.findAllByBranch_BusinessIdOrderByCreatedAtDesc(businessId);
        if (allByBusinessId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByBusinessId);
    }

    public ApiResponse getAllByBranchId(UUID branchId) {
        List<Trade> allByBranchId = tradeRepository.findAllByBranch_IdOrderByCreatedAtDesc(branchId);
        if (allByBranchId.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        return new ApiResponse("found", true, allByBranchId);
    }

    public ApiResponse getTradeByTrader(UUID branchId) {

        List<TradeProduct> tradeList = tradeProductRepository.findAllByTrade_BranchId(branchId);
        if (tradeList.isEmpty()) {
            return new ApiResponse("Not Found", false);
        }

        Map<UUID, Double> traderQuantities = new HashMap<>();

        for (TradeProduct tradeProduct : tradeList) {
            UUID traderId = tradeProduct.getTrade().getTrader().getId();
            Double quantity = traderQuantities.getOrDefault(traderId, 0.0);
            quantity += tradeProduct.getTradedQuantity();
            traderQuantities.put(traderId, quantity);
        }

        List<TraderDto> traderDtoList = new ArrayList<>();

        for (Map.Entry<UUID, Double> entry : traderQuantities.entrySet()) {
            UUID traderId = entry.getKey();
            Optional<User> optionalUser = userRepository.findById(traderId);
            UUID photoId = null;
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                photoId = user.getPhoto().getId();
            }
            String traderName = tradeRepository.getTraderNameById(traderId);
            Double quantitySold = entry.getValue();
            if (photoId != null) {
                traderDtoList.add(new TraderDto(traderId, photoId, traderName, quantitySold));
            } else {
                traderDtoList.add(new TraderDto(traderId, traderName, quantitySold));
            }
        }
        List<TraderDto> sortedTraders = traderDtoList.stream()
                .sorted(Comparator.comparingDouble(TraderDto::getQuantitySold).reversed())
                .toList();

        return new ApiResponse("Found", true, sortedTraders);
    }
}
