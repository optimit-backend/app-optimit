package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.PurchaseProductDto;
import uz.pdp.springsecurity.payload.TradeProductDto;
import uz.pdp.springsecurity.repository.PurchaseProductRepository;
import uz.pdp.springsecurity.repository.WarehouseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WarehouseService {
    @Autowired
    WarehouseRepository warehouseRepository;
    @Autowired
    PurchaseProductRepository purchaseProductRepository;

    public void addPurchase(Purchase purchase) {
        Branch branch = purchase.getBranch();
        List<PurchaseProduct> purchaseProductList = purchaseProductRepository.findAllByPurchaseId(purchase.getId());
        List<Warehouse> warehouseList = new ArrayList<>();

        for (PurchaseProduct purchaseProduct : purchaseProductList) {
            Warehouse warehouse = null;
            if (purchaseProduct.getProduct() != null) {
                Product product = purchaseProduct.getProduct();
                Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch.getId(), product.getId());
                if (optionalWarehouse.isPresent()){
                    warehouse = optionalWarehouse.get();
                    warehouse.setAmount(warehouse.getAmount() + purchaseProduct.getPurchasedQuantity());
                }else {
                    warehouse = new Warehouse();
                    warehouse.setBranch(branch);
                    warehouse.setProduct(product);
                    warehouse.setAmount(purchaseProduct.getPurchasedQuantity());
                }
            } else {
                ProductTypePrice productTypePrice = purchaseProduct.getProductTypePrice();
                Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branch.getId(), productTypePrice.getId());
                if (optionalWarehouse.isPresent()){
                    warehouse = optionalWarehouse.get();
                    warehouse.setAmount(warehouse.getAmount() + purchaseProduct.getPurchasedQuantity());
                }else {
                    warehouse = new Warehouse();
                    warehouse.setBranch(branch);
                    warehouse.setProductTypePrice(productTypePrice);
                    warehouse.setAmount(purchaseProduct.getPurchasedQuantity());
                }
            }
            warehouseList.add(warehouse);
        }
        warehouseRepository.saveAll(warehouseList);
    }

    public Boolean editPurchaseProductAmount(PurchaseProduct purchaseProduct, Double amount) {
        Branch branch = purchaseProduct.getPurchase().getBranch();
        Warehouse warehouse = null;
        if (purchaseProduct.getProduct() != null) {
            Product product = purchaseProduct.getProduct();
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch.getId(), product.getId());
            if (optionalWarehouse.isEmpty())return false;
            warehouse = optionalWarehouse.get();
            warehouse.setAmount(warehouse.getAmount() + amount);
        } else {
            ProductTypePrice productTypePrice = purchaseProduct.getProductTypePrice();
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branch.getId(), productTypePrice.getId());
            if (optionalWarehouse.isEmpty())return false;
            warehouse = optionalWarehouse.get();
            warehouse.setAmount(warehouse.getAmount() + amount);
        }
        warehouseRepository.save(warehouse);
        return true;
    }

    /**
     * RETURN TRADEPRODUCT BY TRADEPRODUCTDTO AFTER CHECK AMOUNT
     * @param branch
     * @param tradeProductDto
     * @return
     */
    public TradeProduct trade(Branch branch, TradeProductDto tradeProductDto) {
        TradeProduct tradeProduct = new TradeProduct();
        if (tradeProductDto.getProductId() != null){
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductId(branch.getId(), tradeProductDto.getProductId());
            if (optionalWarehouse.isEmpty()) return null;
            Warehouse warehouse = optionalWarehouse.get();
            if (warehouse.getAmount() < tradeProductDto.getTradedQuantity()) return null;
            warehouse.setAmount(warehouse.getAmount() - tradeProductDto.getTradedQuantity());
            warehouseRepository.save(warehouse);
            tradeProduct.setProduct(warehouse.getProduct());
        }else {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findByBranchIdAndProductTypePriceId(branch.getId(), tradeProductDto.getProductTypePriceId());
            if (optionalWarehouse.isEmpty()) return null;
            Warehouse warehouse = optionalWarehouse.get();
            if (warehouse.getAmount() < tradeProductDto.getTradedQuantity()) return null;
            warehouse.setAmount(warehouse.getAmount() - tradeProductDto.getTradedQuantity());
            warehouseRepository.save(warehouse);
            tradeProduct.setProductTypePrice(warehouse.getProductTypePrice());
        }
        tradeProduct.setTotalSalePrice(tradeProductDto.getTotalSalePrice());
        tradeProduct.setTradedQuantity(tradeProductDto.getTradedQuantity());
        return tradeProduct;
    }
}
