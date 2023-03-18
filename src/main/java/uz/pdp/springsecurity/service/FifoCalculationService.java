package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.PurchaseDto;
import uz.pdp.springsecurity.payload.PurchaseProductDto;
import uz.pdp.springsecurity.repository.FifoCalculationRepository;
import uz.pdp.springsecurity.repository.PurchaseProductRepository;
import uz.pdp.springsecurity.repository.TradeProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FifoCalculationService {
    @Autowired
    private FifoCalculationRepository fifoRepository;
    @Autowired
    private PurchaseProductRepository purchaseProductRepository;

    public ApiResponse addProduct(Purchase purchase) {
        Branch branch = purchase.getBranch();
        List<PurchaseProduct> purchaseProductList = purchaseProductRepository.findAllByPurchaseId(purchase.getId());

        List<FifoCalculation> fifoCalculationList = new ArrayList<>();

        for (PurchaseProduct purchaseProduct : purchaseProductList) {
            FifoCalculation fifoCalculation = new FifoCalculation(
                    branch,
                    purchaseProduct.getPurchasedQuantity(),
                    purchaseProduct.getPurchasedQuantity(),
                    purchaseProduct.getBuyPrice(),
                    purchase.getDate(),
                    purchase
            );
            if (purchaseProduct.getProduct()!=null){
                fifoCalculation.setProduct(purchaseProduct.getProduct());
            }else {
                fifoCalculation.setProductTypePrice(purchaseProduct.getProductTypePrice());
            }

            fifoCalculationList.add(fifoCalculation);
        }
        fifoRepository.saveAll(fifoCalculationList);
        return new ApiResponse(true);
    }

    public boolean editPurchaseProductAmount(PurchaseProduct purchaseProduct, PurchaseProductDto purchaseProductDto) {
        Purchase purchase = purchaseProduct.getPurchase();
        FifoCalculation fifoCalculation = null;
        if (purchaseProduct.getProduct() != null) {
            Optional<FifoCalculation> optionalFifoCalculation = fifoRepository.findByPurchaseIdAndProductId(purchase.getId(), purchaseProduct.getProduct().getId());
            if (optionalFifoCalculation.isEmpty()) return false;
            fifoCalculation = optionalFifoCalculation.get();
        }else {
            Optional<FifoCalculation> optionalFifoCalculation = fifoRepository.findByPurchaseIdAndProductTypePriceId(purchase.getId(), purchaseProduct.getProductTypePrice().getId());
            if (optionalFifoCalculation.isEmpty()) return false;
            fifoCalculation = optionalFifoCalculation.get();
        }
        fifoCalculation.setBuyPrice(purchaseProduct.getBuyPrice());
        fifoCalculation.setPurchasedAmount(purchaseProduct.getPurchasedQuantity());
        double amount = purchaseProductDto.getPurchasedQuantity() - purchaseProduct.getPurchasedQuantity();
        fifoCalculation.setRemainAmount(fifoCalculation.getRemainAmount() + amount);
        fifoRepository.save(fifoCalculation);
        return true;
    }

    public TradeProduct trade(Branch branch, TradeProduct tradeProduct) {
        List<FifoCalculation> fifoList = null;
        Double salePrice = 0d;
        if (tradeProduct.getProduct() != null) {
            Product product = tradeProduct.getProduct();
            salePrice = product.getSalePrice();
            fifoList = fifoRepository.findAllByBranchIdAndProductIdAndActiveTrueOrderByDateAscCreatedAtAsc(branch.getId(), product.getId());
        } else {
            ProductTypePrice productTypePrice = tradeProduct.getProductTypePrice();
            fifoRepository.findAllByBranchIdAndProductTypePriceIdAndActiveTrueOrderByDateAscCreatedAtAsc(branch.getId(), productTypePrice.getId());
            salePrice = productTypePrice.getSalePrice();
        }

        double tradedQuantity = tradeProduct.getTradedQuantity();
        double profit = 0;
        for (FifoCalculation fifo : fifoList) {
            if (fifo.getRemainAmount()>tradedQuantity){
                fifo.setRemainAmount(fifo.getRemainAmount() - tradedQuantity);
                profit += tradedQuantity * (salePrice - fifo.getBuyPrice());
                break;
            } else if (fifo.getRemainAmount() < tradedQuantity) {
                double amount = fifo.getRemainAmount();
                tradedQuantity -= amount;
                profit += amount * (salePrice - fifo.getBuyPrice());
                fifo.setRemainAmount(0);
                fifo.setActive(false);
            }else {
                profit += tradedQuantity * (salePrice - fifo.getBuyPrice());
                fifo.setRemainAmount(0);
                fifo.setActive(false);
                break;
            }
        }
        tradeProduct.setProfit(profit);
        fifoRepository.saveAll(fifoList);
        return tradeProduct;
    }
}
