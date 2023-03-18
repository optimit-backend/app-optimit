package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.BenefitAndLost;
import uz.pdp.springsecurity.entity.Outlay;
import uz.pdp.springsecurity.entity.Trade;
import uz.pdp.springsecurity.entity.TradeProduct;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.BenefitAndLostDto;
import uz.pdp.springsecurity.repository.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@Service
public class BenefitAndLostService {
    @Autowired
    BranchRepository branchRepository;

    @Autowired
    TradeRepository tradeRepository;

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    OutlayRepository outlayRepository;

    @Autowired
    CurrencyService currencyService;

    @Autowired
    TradeProductRepository tradeProductRepository;

    public ApiResponse findBenefitLost(BenefitAndLostDto benefitAndLostDto) throws ParseException {

        List<Trade> allTrade = tradeRepository.findAllByPayDateIsBetweenAndBranch_Id(benefitAndLostDto.getFirstDate(), benefitAndLostDto.getSecondDate(), benefitAndLostDto.getBranchId());
        if (allTrade.isEmpty()) {
            return new ApiResponse("NOT FOUND", false, "NO INFORMATION WAS FOUND DURING THIS TIME");
        }

        double totalBuySum = 0;
        double totalSaleSum = 0;
        double otherExpenses = 0;
        for (Trade trade : allTrade) {

            for (TradeProduct tradeProduct : tradeProductRepository.findAllByTradeId(trade.getId())) {
                totalBuySum += (tradeProduct.getProduct().getBuyPrice() * tradeProduct.getTradedQuantity());
                totalSaleSum += (tradeProduct.getProduct().getSalePrice() * tradeProduct.getTradedQuantity());
                otherExpenses += (tradeProduct.getProduct().getTax() * tradeProduct.getTradedQuantity());
            }

        }

        List<Outlay> allOptionalOutlay = outlayRepository.findAllByDateIsBetweenAndBranch_Id(benefitAndLostDto.getFirstDate(), benefitAndLostDto.getSecondDate(), benefitAndLostDto.getBranchId());
        for (Outlay outlay : allOptionalOutlay) {
            otherExpenses += outlay.getTotalSum();
        }

        UUID busnessId = allTrade.get(0).getBranch().getBusiness().getId();
        BenefitAndLost benefitAndLost = new BenefitAndLost();

        totalBuySum = currencyService.getValueByActiveCourse(totalBuySum, busnessId);
        benefitAndLost.setTotalBuySum(totalBuySum);
        totalSaleSum = currencyService.getValueByActiveCourse(totalSaleSum, busnessId);
        benefitAndLost.setTotalSaleSum(totalSaleSum);
        otherExpenses = currencyService.getValueByActiveCourse(otherExpenses, busnessId);
        benefitAndLost.setOtherExpenses(otherExpenses);


        if (((totalSaleSum - (totalBuySum + otherExpenses)) > 0)) {
            benefitAndLost.setBenefit(totalSaleSum - (totalBuySum + otherExpenses));
            benefitAndLost.setLost(0);
        } else {
            benefitAndLost.setLost(totalSaleSum - (totalBuySum + otherExpenses));
            benefitAndLost.setBenefit(0);
        }

        return new ApiResponse("FOUND", true, benefitAndLost);
    }

    public ApiResponse findBenefitAndLostByDate(BenefitAndLostDto benefitAndLostDto) throws ParseException {
        System.out.println(benefitAndLostDto.getFirstDate());
        List<Trade> allTrade = tradeRepository.findTradeByOneDate(benefitAndLostDto.getFirstDate());

        if (allTrade.isEmpty()) {
            return new ApiResponse("NOT FOUND", false, "NO INFORMATION WAS FOUND DURING THIS TIME");
        }

        double totalBuySum = 0;
        double totalSaleSum = 0;
        double otherExpenses = 0;
        for (Trade trade : allTrade) {
            for (TradeProduct tradeProduct : tradeProductRepository.findAllByTradeId(trade.getId())) {
                totalBuySum += (tradeProduct.getProduct().getBuyPrice() * tradeProduct.getTradedQuantity());
                totalSaleSum += (tradeProduct.getProduct().getSalePrice() * tradeProduct.getTradedQuantity());
                otherExpenses += (tradeProduct.getProduct().getTax() * tradeProduct.getTradedQuantity());
            }

        }

        List<Outlay> allOptionalOutlay = outlayRepository.findAllByDateAndBranch_Id(benefitAndLostDto.getFirstDate(), benefitAndLostDto.getBranchId());
        if (!allOptionalOutlay.isEmpty()) {
            for (Outlay outlay : allOptionalOutlay) {
                otherExpenses += outlay.getTotalSum();
            }
        }

        UUID busnessId = allTrade.get(0).getBranch().getBusiness().getId();
        BenefitAndLost benefitAndLost = new BenefitAndLost();

        totalBuySum = currencyService.getValueByActiveCourse(totalBuySum, busnessId);
        benefitAndLost.setTotalBuySum(totalBuySum);
        totalSaleSum = currencyService.getValueByActiveCourse(totalSaleSum, busnessId);
        benefitAndLost.setTotalSaleSum(totalSaleSum);
        otherExpenses = currencyService.getValueByActiveCourse(otherExpenses, busnessId);
        benefitAndLost.setOtherExpenses(otherExpenses);


        if (((totalSaleSum - (totalBuySum + otherExpenses)) > 0)) {
            benefitAndLost.setBenefit(totalSaleSum - (totalBuySum + otherExpenses));
            benefitAndLost.setLost(0);
        } else {
            benefitAndLost.setLost(totalSaleSum - (totalBuySum + otherExpenses));
            benefitAndLost.setBenefit(0);
        }

        return new ApiResponse("FOUND", true, benefitAndLost);
    }

    public ApiResponse findBenefitAndLostByWeek(BenefitAndLostDto benefitAndLostDto) throws ParseException {
        Timestamp firstDate = new Timestamp(System.currentTimeMillis());
        Timestamp secondDate = new Timestamp(System.currentTimeMillis() - 604800000);
        List<Trade> tradeList = tradeRepository.findAllByPayDateAndBranchBetween(firstDate, secondDate,benefitAndLostDto.getBranchId());

        if (tradeList.isEmpty()) {
            return new ApiResponse("NOT FOUND", false, "NO INFORMATION WAS FOUND DURING THIS TIME");
        }

        double totalBuySum = 0;
        double totalSaleSum = 0;
        double otherExpenses = 0;
        for (Trade trade : tradeList) {
            for (TradeProduct tradeProduct : tradeProductRepository.findAllByTradeId(trade.getId())) {
                totalBuySum += (tradeProduct.getProduct().getBuyPrice() * tradeProduct.getTradedQuantity());
                totalSaleSum += (tradeProduct.getProduct().getSalePrice() * tradeProduct.getTradedQuantity());
                otherExpenses += (tradeProduct.getProduct().getTax() * tradeProduct.getTradedQuantity());
            }

        }

        List<Outlay> outlayList = outlayRepository.findAllByDateBetweenAndBranchId(firstDate, secondDate,benefitAndLostDto.getBranchId());
        if (!outlayList.isEmpty()) {
            for (Outlay outlay : outlayList) {
                otherExpenses += outlay.getTotalSum();
            }
        }

        UUID busnessId = tradeList.get(0).getBranch().getBusiness().getId();
        BenefitAndLost benefitAndLost = new BenefitAndLost();

        totalBuySum = currencyService.getValueByActiveCourse(totalBuySum, busnessId);
        benefitAndLost.setTotalBuySum(totalBuySum);
        totalSaleSum = currencyService.getValueByActiveCourse(totalSaleSum, busnessId);
        benefitAndLost.setTotalSaleSum(totalSaleSum);
        otherExpenses = currencyService.getValueByActiveCourse(otherExpenses, busnessId);
        benefitAndLost.setOtherExpenses(otherExpenses);


        if (((totalSaleSum - (totalBuySum + otherExpenses)) > 0)) {
            benefitAndLost.setBenefit(totalSaleSum - (totalBuySum + otherExpenses));
            benefitAndLost.setLost(0);
        } else {
            benefitAndLost.setLost(totalSaleSum - (totalBuySum + otherExpenses));
            benefitAndLost.setBenefit(0);
        }

        return new ApiResponse("FOUND",true,benefitAndLost);
    }
}
