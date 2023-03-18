package uz.pdp.springsecurity.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Customer;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.TradeDTO;
import uz.pdp.springsecurity.payload.TradeProductDto;
import uz.pdp.springsecurity.repository.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TradeService {
    @Autowired
    TradeRepository tradeRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ProductRepository productRepository;

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

    @SneakyThrows
    public ApiResponse create(TradeDTO tradeDTO) {
        Trade trade = new Trade();


        return addTrade(trade, tradeDTO);
    }

    public ApiResponse addTrade(Trade trade, TradeDTO tradeDTO) {

        /**
         * SET LATER
         */
        Optional<Customer> optionalCustomer = customerRepository.findById(tradeDTO.getCustomerId());
        if (optionalCustomer.isEmpty()) {
            return new ApiResponse("CUSTOMER NOT FOUND", false);
        }
        Customer customer = optionalCustomer.get();
        trade.setCustomer(customer);

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
        if(optionalPaymentStatus.isEmpty()){
            return new ApiResponse("PAYMENTSTATUS NOT FOUND", false);
        }
        trade.setPaymentStatus(optionalPaymentStatus.get());

        Optional<PaymentMethod> optionalPaymentMethod = payMethodRepository.findById(tradeDTO.getPayMethodId());
        if (optionalPaymentMethod.isEmpty()) {
            return new ApiResponse("PAYMAENTMETHOD NOT FOUND", false);
        }
        trade.setPayMethod(optionalPaymentMethod.get());

        double loanSum = tradeDTO.getDebtSum();
        if (loanSum > 0) {
            customer.setDebt(customer.getDebt() + loanSum);
        }

        trade.setPayDate(tradeDTO.getPayDate());
        trade.setTotalSum(tradeDTO.getTotalSum());
        trade.setPaidSum(tradeDTO.getPaidSum());
        trade.setDebtSum(loanSum);
        tradeRepository.save(trade);

        /**
         * SOTILGAN PRODUCT SAQLANDI YANI TRADERPRODUCT
         */
        List<TradeProductDto> productTraderDto = tradeDTO.getProductTraderDto();
        List<TradeProduct> tradeProductList = new ArrayList<>();

        double profit = 0;
        for (TradeProductDto tradeProductDto : productTraderDto) {
            TradeProduct tradeProduct = warehouseService.trade(branch, tradeProductDto);
            if (tradeProduct != null) {
                tradeProduct.setTrade(trade);
                TradeProduct savedTradeProduct = fifoCalculationService.trade(branch, tradeProduct);
                tradeProductList.add(savedTradeProduct);
                profit += savedTradeProduct.getProfit();
            }
        }
        trade.setTotalProfit(profit);
        tradeRepository.save(trade);
        tradeProductRepository.saveAll(tradeProductList);
        return new ApiResponse("SAVED!", true);
    }

    public ApiResponse edit(UUID id, TradeDTO tradeDTO) {
        Optional<Trade> optionalTrade = tradeRepository.findById(id);
        if (optionalTrade.isEmpty()) {
            return new ApiResponse("NOT FOUND TRADE", false);
        }
        Trade trade = optionalTrade.get();
        ApiResponse apiResponse = addTrade(trade, tradeDTO);

        if (!apiResponse.isSuccess()) {
            return new ApiResponse("ERROR", false);
        }
        return new ApiResponse("UPDATED", true);
    }

    public ApiResponse getOne(UUID id) {
        Optional<Trade> optionalTrade = tradeRepository.findById(id);
        if (optionalTrade.isEmpty()){
            return new ApiResponse("NOT FOUND", false);
        }
        Trade trade = generateTradeByActiveCourse(optionalTrade.get());
        return  new ApiResponse(true, trade);
    }

    public ApiResponse deleteTrade(UUID id) {
        Optional<Trade> byId = tradeRepository.findById(id);
        if (byId.isEmpty()) return new ApiResponse("NOT FOUND",false);
        tradeRepository.deleteById(id);
        return new ApiResponse("DELATED", true);
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
        List<Trade> tradeList = new ArrayList<>();
        for (Trade trade : allByTrader_id) {
            Trade generateTradeByActiveCourse = generateTradeByActiveCourse(trade);
            tradeList.add(generateTradeByActiveCourse);
        }
        return new ApiResponse("FOUND", true, tradeList);
    }

    public ApiResponse getAllByBranchId(UUID branch_id) {
        List<Trade> allByBranch_id = tradeRepository.findAllByBranch_Id(branch_id);
        if (allByBranch_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        List<Trade> tradeList = new ArrayList<>();
        for (Trade trade : allByBranch_id) {
            Trade generateTradeByActiveCourse = generateTradeByActiveCourse(trade);
            tradeList.add(generateTradeByActiveCourse);
        }
        return new ApiResponse("FOUND", true, tradeList);
    }

    public ApiResponse getByCustomerId(UUID customer_id) {
        List<Trade> allByCustomer_id = tradeRepository.findAllByCustomer_Id(customer_id);
        if (allByCustomer_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        List<Trade> tradeList = new ArrayList<>();
        for (Trade trade : allByCustomer_id) {
            Trade generateTradeByActiveCourse = generateTradeByActiveCourse(trade);
            tradeList.add(generateTradeByActiveCourse);
        }
        return new ApiResponse("FOUND", true, tradeList);
    }

    public ApiResponse getByPayDate(Timestamp payDate) throws ParseException {
        List<Trade> allByPayDate = tradeRepository.findTradeByOneDate(payDate);
        if (allByPayDate.isEmpty()) return new ApiResponse("NOT FOUND", false);
        List<Trade> tradeList = new ArrayList<>();
        for (Trade trade : allByPayDate) {
            Trade trade1 = generateTradeByActiveCourse(trade);
            tradeList.add(trade1);
        }
        return new ApiResponse("FOUND", true, tradeList);
    }

    public ApiResponse getByPayStatus(UUID paymentStatus_id) {
        List<Trade> allByPaymentStatus_id = tradeRepository.findAllByPaymentStatus_Id(paymentStatus_id);
        if (allByPaymentStatus_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        List<Trade> tradeList = new ArrayList<>();
        for (Trade trade : allByPaymentStatus_id) {
            Trade generateTradeByActiveCourse = generateTradeByActiveCourse(trade);
            tradeList.add(generateTradeByActiveCourse);
        }
        return new ApiResponse("FOUND", true, tradeList);
    }

    public ApiResponse getByPayMethod(UUID payMethod_id) {
        List<Trade> allByPaymentMethod_id = tradeRepository.findAllByPayMethod_Id(payMethod_id);
        if (allByPaymentMethod_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        List<Trade> tradeList = new ArrayList<>();
        for (Trade trade : allByPaymentMethod_id) {
            Trade trade1 = generateTradeByActiveCourse(trade);
            tradeList.add(trade1);
        }
        return new ApiResponse("FOUND", true, tradeList);
    }

    public ApiResponse getByAddress(UUID address_id) {
        List<Trade> allByAddress_id = tradeRepository.findAllByAddress_Id(address_id);
        if (allByAddress_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        List<Trade> tradeList = new ArrayList<>();
        for (Trade trade : allByAddress_id) {
            Trade trade1 = generateTradeByActiveCourse(trade);
            tradeList.add(trade1);
        }
        return new ApiResponse("FOUND", true, tradeList);
    }

    public ApiResponse createPdf(UUID id, HttpServletResponse response) throws IOException {

        Optional<Trade> tradeOptional = tradeRepository.findById(id);
        PDFService pdfService = new PDFService();

        pdfService.createPdf(tradeOptional.get(), response);

        return new ApiResponse("CREATED", true);
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        List<Trade> allByBusinessId = tradeRepository.findAllByBusinessId(businessId);
        if (allByBusinessId.isEmpty()) return new ApiResponse("NOT FOUND",false);
        List<Trade> tradeList = new ArrayList<>();
        for (Trade trade : allByBusinessId) {
            Trade trade1 = generateTradeByActiveCourse(trade);
            tradeList.add(trade1);
        }
        return new ApiResponse("FOUND",true,tradeList);
    }

    private Trade generateTradeByActiveCourse(Trade trade){
        UUID busnessId = trade.getBranch().getBusiness().getId();
        double avans = currencyService.getValueByActiveCourse(trade.getPaidSum(), busnessId);
        trade.setPaidSum(avans);
        double totalSum = currencyService.getValueByActiveCourse(trade.getTotalSum(), busnessId);
        trade.setTotalSum(totalSum);
//        sac


        for (TradeProduct tradeProduct : tradeProductRepository.findAllByTradeId(trade.getId())) {
            double salePrice = currencyService.getValueByActiveCourse(tradeProduct.getTotalSalePrice(), busnessId);
            tradeProduct.setTotalSalePrice(salePrice);
            Product product = tradeProduct.getProduct();
            product.setSalePrice(salePrice);
            double buyPrice = currencyService.getValueByActiveCourse(product.getBuyPrice(), busnessId);
            product.setBuyPrice(buyPrice);
        }
        return trade;
    }
}
