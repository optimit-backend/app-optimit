package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.Type;
import uz.pdp.springsecurity.repository.FifoCalculationRepository;
import uz.pdp.springsecurity.repository.ProductTypeComboRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FifoCalculationService {
    private final FifoCalculationRepository fifoRepository;
    private final ProductTypeComboRepository productTypeComboRepository;

    public void createPurchaseProduct(PurchaseProduct purchaseProduct) {
        FifoCalculation fifoCalculation = new FifoCalculation(
                purchaseProduct.getPurchase().getBranch(),
                purchaseProduct.getPurchasedQuantity(),
                purchaseProduct.getPurchasedQuantity(),
                purchaseProduct.getBuyPrice(),
                purchaseProduct.getPurchase().getDate(),
                purchaseProduct
        );
        if (purchaseProduct.getProduct()!=null){
            fifoCalculation.setProduct(purchaseProduct.getProduct());
        }else {
            fifoCalculation.setProductTypePrice(purchaseProduct.getProductTypePrice());
        }
        fifoRepository.save(fifoCalculation);
    }

    public void createProduction(Production production) {
        FifoCalculation fifoCalculation = new FifoCalculation(
                production.getBranch(),
                production.getQuantity(),
                production.getQuantity(),
                production.getTotalPrice() / production.getQuantity(),
                production.getDate(),
                production
        );
        if (production.getProduct()!=null){
            fifoCalculation.setProduct(production.getProduct());
        }else {
            fifoCalculation.setProductTypePrice(production.getProductTypePrice());
        }
        fifoRepository.save(fifoCalculation);
    }

    public void editPurchaseProduct(PurchaseProduct purchaseProduct, Double amount) {
        Optional<FifoCalculation> optionalFifoCalculation = fifoRepository.findByPurchaseProductId(purchaseProduct.getId());
        if (optionalFifoCalculation.isEmpty()) return;
        FifoCalculation fifoCalculation = optionalFifoCalculation.get();
        fifoCalculation.setBuyPrice(purchaseProduct.getBuyPrice());
        fifoCalculation.setPurchasedAmount(purchaseProduct.getPurchasedQuantity());
        fifoCalculation.setRemainAmount(fifoCalculation.getRemainAmount() + amount);
        if (fifoCalculation.getRemainAmount() <= 0d)fifoCalculation.setActive(false);
        fifoRepository.save(fifoCalculation);
    }

    public TradeProduct createOrEditTradeProduct(Branch branch, TradeProduct tradeProduct, double quantity) {
        List<FifoCalculation> fifoList;
        double totalBuyPrice = 0;
        if (tradeProduct.getProduct() != null) {
            Product product = tradeProduct.getProduct();
            if (product.getType().equals(Type.SINGLE)) {
                fifoList = fifoRepository.findAllByBranchIdAndProductIdAndActiveTrueOrderByDate(branch.getId(), product.getId());
                totalBuyPrice = createOrEditTradeProductHelper(fifoList, quantity, branch.getBusiness().getSaleMinus());
                fifoRepository.saveAll(fifoList);
            }else {
                List<ProductTypeCombo> comboList = productTypeComboRepository.findAllByMainProductId(product.getId());
                for (ProductTypeCombo combo : comboList) {
                    fifoList = fifoRepository.findAllByBranchIdAndProductIdAndActiveTrueOrderByDate(branch.getId(), combo.getContentProduct().getId());
                    totalBuyPrice += createOrEditTradeProductHelper(fifoList, quantity * combo.getAmount(), branch.getBusiness().getSaleMinus());
                    fifoRepository.saveAll(fifoList);
                }
            }
        } else {
            ProductTypePrice productTypePrice = tradeProduct.getProductTypePrice();
            fifoList = fifoRepository.findAllByBranchIdAndProductTypePriceIdAndActiveTrueOrderByDate(branch.getId(), productTypePrice.getId());
            totalBuyPrice = createOrEditTradeProductHelper(fifoList, quantity, branch.getBusiness().getSaleMinus());
            fifoRepository.saveAll(fifoList);
        }

        double totalSalePrice = tradeProduct.getTotalSalePrice() * quantity / tradeProduct.getTradedQuantity();
        tradeProduct.setProfit(tradeProduct.getProfit() + (totalSalePrice - totalBuyPrice));
        return tradeProduct;
    }

    private Double createOrEditTradeProductHelper(List<FifoCalculation> fifoList, double quantity, boolean saleMinus) {
        double buyPrice = 0;
        double totalBuyPrice = 0;
        for (FifoCalculation fifo : fifoList) {
            if (fifo.getRemainAmount()>quantity){
                fifo.setRemainAmount(fifo.getRemainAmount() - quantity);
                buyPrice = fifo.getBuyPrice();
                totalBuyPrice += quantity * buyPrice;
                quantity = 0;
                break;
            } else if (fifo.getRemainAmount() < quantity) {
                buyPrice = fifo.getBuyPrice();
                quantity -= fifo.getRemainAmount();
                totalBuyPrice += fifo.getRemainAmount() * buyPrice;
                fifo.setRemainAmount(0);
                fifo.setActive(false);
            }else {
                buyPrice = fifo.getBuyPrice();
                totalBuyPrice += quantity * buyPrice;
                fifo.setRemainAmount(0);
                fifo.setActive(false);
                quantity = 0;
                break;
            }
        }
        if (saleMinus && quantity > 0){
            totalBuyPrice += quantity * buyPrice;
        }
        return totalBuyPrice;
    }

    public void returnedTrade(Branch branch, TradeProduct tradeProduct, double quantity) {
        List<FifoCalculation> fifoList;
        double totalBuyPrice = 0;
        if (tradeProduct.getProduct() != null) {
            Product product = tradeProduct.getProduct();
            if (product.getType().equals(Type.SINGLE)) {
                fifoList = fifoRepository.findFirst20ByBranchIdAndProductIdOrderByDateDesc(branch.getId(), product.getId());
                totalBuyPrice = returnedTradeHelper(fifoList, quantity);
                fifoRepository.saveAll(fifoList);
            }else {
                List<ProductTypeCombo> comboList = productTypeComboRepository.findAllByMainProductId(product.getId());
                for (ProductTypeCombo combo : comboList) {
                    fifoList = fifoRepository.findFirst20ByBranchIdAndProductIdOrderByDateDesc(branch.getId(), combo.getContentProduct().getId());
                    totalBuyPrice += returnedTradeHelper(fifoList, quantity * combo.getAmount());
                    fifoRepository.saveAll(fifoList);
                }
            }
        } else {
            ProductTypePrice productTypePrice = tradeProduct.getProductTypePrice();
            fifoList = fifoRepository.findFirst20ByBranchIdAndProductTypePriceIdOrderByDateDesc(branch.getId(), productTypePrice.getId());
            totalBuyPrice = returnedTradeHelper(fifoList, quantity);
            fifoRepository.saveAll(fifoList);
        }
        double totalSalePrice = tradeProduct.getTotalSalePrice() * quantity / tradeProduct.getTradedQuantity();
        tradeProduct.setProfit(tradeProduct.getProfit() - (totalSalePrice - totalBuyPrice));
    }

    private Double returnedTradeHelper(List<FifoCalculation> fifoList, Double quantity) {
        double totalBuyPrice = 0;
        for (FifoCalculation fifo : fifoList) {
            if (fifo.getPurchasedAmount() == fifo.getRemainAmount())continue;
            double soldQuantity = fifo.getPurchasedAmount() - fifo.getRemainAmount();
            if (soldQuantity >= quantity) {
                fifo.setRemainAmount(fifo.getRemainAmount() + quantity);
                fifo.setActive(true);
                totalBuyPrice += quantity * fifo.getBuyPrice();
                return totalBuyPrice;
            } else {
                quantity -= soldQuantity;
                fifo.setRemainAmount(fifo.getPurchasedAmount());
                fifo.setActive(true);
                totalBuyPrice += soldQuantity * fifo.getBuyPrice();
            }
        }
        return totalBuyPrice;
    }

    public ContentProduct createContentProduct(Branch branch, ContentProduct contentProduct) {
        List<FifoCalculation> fifoList;
        if (contentProduct.getProduct() != null) {
            Product product = contentProduct.getProduct();
            fifoList = fifoRepository.findAllByBranchIdAndProductIdAndActiveTrueOrderByDate(branch.getId(), product.getId());
            contentProduct.setTotalPrice(createContentProductHelper(fifoList, contentProduct.getQuantity(), branch.getBusiness().getSaleMinus()));
            fifoRepository.saveAll(fifoList);
        } else {
            ProductTypePrice productTypePrice = contentProduct.getProductTypePrice();
            fifoList = fifoRepository.findAllByBranchIdAndProductTypePriceIdAndActiveTrueOrderByDate(branch.getId(), productTypePrice.getId());
            contentProduct.setTotalPrice(createContentProductHelper(fifoList, contentProduct.getQuantity(), branch.getBusiness().getSaleMinus()));
            fifoRepository.saveAll(fifoList);
        }
        return contentProduct;
    }

    private Double createContentProductHelper(List<FifoCalculation> fifoList, Double quantity, boolean saleMinus) {
        double buyPrice = 0;
        double totalBuyPrice = 0;
        for (FifoCalculation fifo : fifoList) {
            if (fifo.getRemainAmount()>quantity){
                buyPrice = fifo.getBuyPrice();
                fifo.setRemainAmount(fifo.getRemainAmount() - quantity);
                totalBuyPrice += quantity * buyPrice;
                break;
            } else if (fifo.getRemainAmount() < quantity) {
                buyPrice = fifo.getBuyPrice();
                double amount = fifo.getRemainAmount();
                quantity -= amount;
                totalBuyPrice += amount * buyPrice;
                fifo.setRemainAmount(0);
                fifo.setActive(false);
            }else {
                buyPrice = fifo.getBuyPrice();
                totalBuyPrice += quantity * buyPrice;
                fifo.setRemainAmount(0);
                fifo.setActive(false);
                break;
            }
        }
        if (saleMinus && quantity > 0){
            totalBuyPrice += quantity * buyPrice;
        }
        return totalBuyPrice;
    }

    public void createExchange(ExchangeProductBranch exchangeProductBranch) {
        for (ExchangeProduct exchangeProduct : exchangeProductBranch.getExchangeProductList()) {
            if (exchangeProduct.getProduct() != null) {
                List<FifoCalculation> fifoCalculationList = fifoRepository.findAllByBranchIdAndProductIdAndActiveTrueOrderByDate(
                        exchangeProductBranch.getShippedBranch().getId(), exchangeProduct.getProduct().getId());
                createExchangeHelper(fifoCalculationList, exchangeProduct);

                FifoCalculation fifoCalculation = new FifoCalculation();
                fifoCalculation.setBranch(exchangeProductBranch.getReceivedBranch());
                fifoCalculation.setDate(exchangeProductBranch.getExchangeDate());
                fifoCalculation.setPurchasedAmount(exchangeProduct.getExchangeProductQuantity());
                fifoCalculation.setRemainAmount(exchangeProduct.getExchangeProductQuantity());
                fifoCalculation.setProduct(exchangeProduct.getProduct());
                fifoCalculation.setBuyPrice(exchangeProduct.getProduct().getBuyPrice());
                fifoRepository.save(fifoCalculation);
            } else {
                List<FifoCalculation> fifoCalculationList = fifoRepository.findAllByBranchIdAndProductTypePriceIdAndActiveTrueOrderByDate(
                        exchangeProductBranch.getShippedBranch().getId(), exchangeProduct.getProductTypePrice().getId());
                createExchangeHelper(fifoCalculationList, exchangeProduct);

                FifoCalculation fifoCalculation = new FifoCalculation();
                fifoCalculation.setBranch(exchangeProductBranch.getReceivedBranch());
                fifoCalculation.setDate(exchangeProductBranch.getExchangeDate());
                fifoCalculation.setPurchasedAmount(exchangeProduct.getExchangeProductQuantity());
                fifoCalculation.setRemainAmount(exchangeProduct.getExchangeProductQuantity());
                fifoCalculation.setProductTypePrice(exchangeProduct.getProductTypePrice());
                fifoCalculation.setBuyPrice(exchangeProduct.getProductTypePrice().getBuyPrice());
                fifoRepository.save(fifoCalculation);
            }
        }

    }

    private void createExchangeHelper(List<FifoCalculation> fifoCalculationList, ExchangeProduct exchangeProduct) {
        Double quantity = exchangeProduct.getExchangeProductQuantity();
        for (FifoCalculation fifoCalculation : fifoCalculationList) {
            if (fifoCalculation.getRemainAmount()>quantity){
                fifoCalculation.setRemainAmount(fifoCalculation.getRemainAmount() - quantity);
                break;
            } else if (fifoCalculation.getRemainAmount() < quantity) {
                double amount = fifoCalculation.getRemainAmount();
                quantity -= amount;
                fifoCalculation.setRemainAmount(0);
                fifoCalculation.setActive(false);
            }else {
                fifoCalculation.setRemainAmount(0);
                fifoCalculation.setActive(false);
                break;
            }
        }
        fifoRepository.saveAll(fifoCalculationList);
    }


}
