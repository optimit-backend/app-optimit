package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PurchaseService {
    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    PurchaseProductRepository purchaseProductRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    ExchangeStatusRepository exchangeStatusRepository;

    @Autowired
    PaymentStatusRepository paymentStatusRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CurrentCourceRepository currentCourceRepository;

    @Autowired
    FifoCalculationService fifoCalculationService;

    @Autowired
    ProductTypePriceRepository productTypePriceRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    WarehouseService warehouseService;


    public ApiResponse add(PurchaseDto purchaseDto) {
        Purchase purchase = new Purchase();
        return createOrEditPurchase(purchase, purchaseDto);
    }

    public ApiResponse edit(UUID id, PurchaseDto purchaseDto) {
        Optional<Purchase> optionalPurchase = purchaseRepository.findById(id);
        if (optionalPurchase.isEmpty()) return new ApiResponse("NOT FOUND", false);

        Purchase purchase = optionalPurchase.get();
        if (!purchase.isEditable()) return new ApiResponse("YOU CAN NOT EDIT AFTER 24 HOUR", false);
        Timestamp createdAt = purchase.getCreatedAt();
        long difference = System.currentTimeMillis() - createdAt.getTime();
        long oneDay = 1000 * 60 * 60 * 24;
        if (difference > oneDay){
            purchase.setEditable(false);
            return new ApiResponse("YOU CAN NOT EDIT AFTER 24 HOUR", false);
        }
        return createOrEditPurchase(purchase, purchaseDto);
    }

    private ApiResponse createOrEditPurchase(Purchase purchase, PurchaseDto purchaseDto) {
        Optional<Supplier> optionalSupplier = supplierRepository.findById(purchaseDto.getSupplerId());
        if (optionalSupplier.isEmpty()) return new ApiResponse("SUPPLIER NOT FOUND", false);
        Supplier supplier = optionalSupplier.get();
        purchase.setSupplier(supplier);

        Optional<User> optionalUser = userRepository.findById(purchaseDto.getSeller());
        if (optionalUser.isEmpty()) return new ApiResponse("SELLER NOT FOUND", false);
        purchase.setSeller(optionalUser.get());

        Optional<ExchangeStatus> optionalPurchaseStatus = exchangeStatusRepository.findById(purchaseDto.getPurchaseStatusId());
        if (optionalPurchaseStatus.isEmpty()) return new ApiResponse("PURCHASE STATUS NOT FOUND", false);
        purchase.setPurchaseStatus(optionalPurchaseStatus.get());

        Optional<PaymentStatus> optionalPaymentStatus = paymentStatusRepository.findById(purchaseDto.getPaymentStatusId());
        if (optionalPaymentStatus.isEmpty()) return new ApiResponse("PAYMENT STATUS NOT FOUND", false);
        purchase.setPaymentStatus(optionalPaymentStatus.get());

        Optional<Branch> optionalBranch = branchRepository.findById(purchaseDto.getBranchId());
        if (optionalBranch.isEmpty()) return new ApiResponse("BRANCH NOT FOUND", false);
        Branch branch = optionalBranch.get();
        purchase.setBranch(branch);

        double debtSum = purchase.getDebtSum();
        if (purchaseDto.getDebtSum() > 0 || debtSum != purchase.getDebtSum()) {
            supplier.setDebt(supplier.getDebt() - debtSum + purchaseDto.getDebtSum());
            supplierRepository.save(supplier);
        }

        purchase.setTotalSum(purchaseDto.getTotalSum());
        purchase.setPaidSum(purchaseDto.getPaidSum());
        purchase.setDebtSum(purchaseDto.getDebtSum());
        purchase.setDeliveryPrice(purchaseDto.getDeliveryPrice());
        purchase.setDate(purchaseDto.getDate());
        purchase.setDescription(purchaseDto.getDescription());

        purchaseRepository.save(purchase);

        List<PurchaseProductDto> purchaseProductDtoList = purchaseDto.getPurchaseProductsDto();
        List<PurchaseProduct> purchaseProductList = new ArrayList<>();

        for (PurchaseProductDto purchaseProductDto : purchaseProductDtoList) {
            if (purchaseProductDto.getPurchaseProductId() == null){
                PurchaseProduct purchaseProduct = createOrEditPurchaseProduct(new PurchaseProduct(), purchaseProductDto);
                if (purchaseProduct == null)continue;
                purchaseProduct.setPurchase(purchase);
                purchaseProductRepository.save(purchaseProduct);
                purchaseProductList.add(purchaseProduct);
                fifoCalculationService.createPurchaseProduct(purchaseProduct);
                warehouseService.createOrEditWareHouse(purchaseProduct, purchaseProduct.getPurchasedQuantity());
            } else if (purchaseProductDto.isDelete()){
                if(purchaseProductRepository.existsById(purchaseProductDto.getPurchaseProductId())){
                    PurchaseProduct purchaseProduct = purchaseProductRepository.getById(purchaseProductDto.getPurchaseProductId());
                    warehouseService.createOrEditWareHouse(purchaseProduct, - purchaseProduct.getPurchasedQuantity());
                    purchaseProductRepository.deleteById(purchaseProductDto.getPurchaseProductId());
                }
            } else {
                Optional<PurchaseProduct> optionalPurchaseProduct = purchaseProductRepository.findById(purchaseProductDto.getPurchaseProductId());
                if (optionalPurchaseProduct.isEmpty()) continue;
                PurchaseProduct purchaseProduct = optionalPurchaseProduct.get();
                double amount = purchaseProductDto.getPurchasedQuantity() - purchaseProduct.getPurchasedQuantity();

                PurchaseProduct editPurchaseProduct = createOrEditPurchaseProduct(purchaseProduct, purchaseProductDto);
                if (editPurchaseProduct == null)continue;
                editPurchaseProduct.setPurchase(purchase);
                purchaseProductList.add(editPurchaseProduct);

                if (amount != 0) {
                    purchaseProduct.setPurchasedQuantity(purchaseProductDto.getPurchasedQuantity());
                    fifoCalculationService.editPurchaseProduct(purchaseProduct, amount);
                    warehouseService.createOrEditWareHouse(purchaseProduct, amount);
                }
            }
        }
        purchaseProductRepository.saveAll(purchaseProductList);

        return new ApiResponse("SUCCESS", true);
    }

    private PurchaseProduct createOrEditPurchaseProduct(PurchaseProduct purchaseProduct, PurchaseProductDto purchaseProductDto) {
        //SINGLE TYPE
        if (purchaseProductDto.getProductId()!=null) {
            UUID productId = purchaseProductDto.getProductId();
            Optional<Product> optional = productRepository.findById(productId);
            if (optional.isEmpty())return null;
            Product product = optional.get();
            product.setSalePrice(purchaseProductDto.getSalePrice());
            product.setBuyPrice(purchaseProductDto.getBuyPrice());
            productRepository.save(product);
            purchaseProduct.setProduct(product);
        } else {//MANY TYPE
            UUID productTypePriceId = purchaseProductDto.getProductTypePriceId();
            Optional<ProductTypePrice> optional = productTypePriceRepository.findById(productTypePriceId);
            if (optional.isEmpty())return null;
            ProductTypePrice productTypePrice = optional.get();
            productTypePrice.setBuyPrice(purchaseProductDto.getBuyPrice());
            productTypePrice.setSalePrice(purchaseProductDto.getSalePrice());
            productTypePriceRepository.save(productTypePrice);
            purchaseProduct.setProductTypePrice(productTypePrice);
        }

        purchaseProduct.setPurchasedQuantity(purchaseProductDto.getPurchasedQuantity());
        purchaseProduct.setSalePrice(purchaseProductDto.getSalePrice());
        purchaseProduct.setBuyPrice(purchaseProductDto.getBuyPrice());
        purchaseProduct.setTotalSum(purchaseProductDto.getTotalSum());

        return purchaseProduct;
    }

    public ApiResponse getAllByBusiness(UUID businessId) {
        List<Purchase> purchaseList = purchaseRepository.findAllByBranch_BusinessId(businessId);
        if (purchaseList.isEmpty()) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, purchaseList);

        /*if (allByBusinessId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        List<Purchase> purchaseList = new ArrayList<>();
        for (Purchase purchase : allByBusinessId) {
            Purchase changePrices = changePrices(purchase);
            purchaseList.add(changePrices);
        }
        return new ApiResponse("FOUND", true, purchaseList);*/
    }

    /**
     * check get one
     * @param id
     * @return
     */
    public ApiResponse getOne(UUID id) {
        if (!purchaseRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);
        Purchase purchase = purchaseRepository.findById(id).get();
        List<PurchaseProduct> purchaseProductList = purchaseProductRepository.findAllByPurchaseId(purchase.getId());
        if (purchaseProductList.isEmpty()) return new ApiResponse("NOT FOUND PRODUCTS", false);
        PurchaseGetOneDto purchaseGetOneDto = new PurchaseGetOneDto();
        purchaseGetOneDto.setPurchase(purchase);
        purchaseGetOneDto.setPurchaseProductList(purchaseProductList);
        return new ApiResponse("FOUND", true, purchaseGetOneDto);
    }

    public ApiResponse delete(UUID id) {
        if (!purchaseRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);
        purchaseRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getByDealerId(UUID dealer_id) {
        List<Purchase> allByDealer_id = purchaseRepository.findAllBySupplierId(dealer_id);
        if (allByDealer_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByDealer_id);

        /*List<Purchase> purchaseList = new ArrayList<>();
        for (Purchase purchase : allByDealer_id) {
            Purchase changePrices = changePrices(purchase);
            purchaseList.add(changePrices);
        }
        return new ApiResponse("FOUND", true, purchaseList);*/
    }

    public ApiResponse getByPurchaseStatusId(UUID purchaseStatus_id) {
        List<Purchase> allByPurchaseStatus_id = purchaseRepository.findAllByPurchaseStatus_Id(purchaseStatus_id);
        if (allByPurchaseStatus_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByPurchaseStatus_id);

        /*List<Purchase> purchaseList = new ArrayList<>();
        for (Purchase purchase : allByPurchaseStatus_id) {
            Purchase changePrices = changePrices(purchase);
            purchaseList.add(changePrices);
        }
        return new ApiResponse("FOUND", true, purchaseList);*/
    }

    public ApiResponse getByPaymentStatusId(UUID paymentStatus_id) {
        List<Purchase> allByPaymentStatus_id = purchaseRepository.findAllByPaymentStatus_Id(paymentStatus_id);
        if (allByPaymentStatus_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByPaymentStatus_id);

        /*List<Purchase> purchaseList = new ArrayList<>();
        for (Purchase purchase : allByPaymentStatus_id) {
            Purchase changePrices = changePrices(purchase);
            purchaseList.add(changePrices);
        }
        return new ApiResponse("FOUND", true, purchaseList);*/
    }

    public ApiResponse getByBranchId(UUID branch_id) {
        List<Purchase> allByBranch_id = purchaseRepository.findAllByBranch_Id(branch_id);
        if (allByBranch_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByBranch_id);

        /*List<Purchase> purchaseList = new ArrayList<>();
        for (Purchase purchase : allByBranch_id) {
            Purchase changePrices = changePrices(purchase);
            purchaseList.add(changePrices);
        }
        return new ApiResponse("FOUND", true, purchaseList);*/
    }

    public ApiResponse getByDate(Date date) {
        List<Purchase> allByDate = purchaseRepository.findAllByDate(date);
        if (allByDate.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByDate);

        /*List<Purchase> purchaseList = new ArrayList<>();
        for (Purchase purchase : allByDate) {
            Purchase changePrices = changePrices(purchase);
            purchaseList.add(changePrices);
        }
        return new ApiResponse("FOUND", true, purchaseList);*/
    }

    public ApiResponse getPdfFile(UUID id, HttpServletResponse response) throws IOException {
        Optional<Purchase> optionalPurchase = purchaseRepository.findById(id);
        if (optionalPurchase.isEmpty()) {
            return new ApiResponse("NOT FOUND PURCHASE", false);
        }
        PDFService pdfService = new PDFService();
        pdfService.createPdfPurchase(optionalPurchase.get(), response);
        return new ApiResponse("CREATED", true);
    }

    /*public ApiResponse getByTotalSum(double totalSum) {
        List<Purchase> allByTotalSum = purchaseRepository.findAllByTotalSum(totalSum);
        if (allByTotalSum.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByTotalSum);

        *//*List<Purchase> purchaseList = new ArrayList<>();
        for (Purchase purchase : allByTotalSum) {
            Purchase changePrices = changePrices(purchase);
            purchaseList.add(changePrices);
        }
        return new ApiResponse("FOUND", true, purchaseList);*//*
    }*/

    /*public ApiResponse getCostByBusiness(UUID businessId) {
        double cost = 0;
        double debt = 0;
        List<Purchase> purchaseList = purchaseRepository.findAllByBusinessId(businessId);
        for (Purchase purchase : purchaseList) {
            if (purchase.getTotalSum()>=purchase.getPaidSum() && purchase.getPaidSum()!=0){
                cost += purchase.getPaidSum();
                debt += (purchase.getTotalSum() - purchase.getPaidSum());
            }else {
                debt += purchase.getTotalSum();
            }
        }
        Statistic statistic = new Statistic(cost, debt);
        return new ApiResponse("Succesly", true, statistic);
    }*/

    /*private Purchase changePrices(Purchase purchase){
        Currency currency = currencyRepository.findByBusinessIdAndActiveTrue(purchase.getBranch().getBusiness().getId());
        CurrentCource course = currentCourceRepository.getByCurrencyIdAndActive(currency.getId(), true);
        if (!currency.getName().equalsIgnoreCase("SO'M")){
            double deliveryPrice = purchase.getDeliveryPrice();
            deliveryPrice = deliveryPrice / course.getCurrentCourse();
            purchase.setDeliveryPrice(deliveryPrice);
            double avans = purchase.getPaidSum();
            avans = avans / course.getCurrentCourse();
            purchase.setPaidSum(avans);
            double totalSum = purchase.getTotalSum();
            totalSum = totalSum / course.getCurrentCourse();
            purchase.setTotalSum(totalSum);
            List<PurchaseProduct> productList = purchaseProductRepository.findAllByPurchaseId(purchase.getId());
            for (PurchaseProduct product : productList) {
                double salePrice = product.getSalePrice();
                salePrice = salePrice / course.getCurrentCourse();
                product.setSalePrice(salePrice);
                double buyPrice = product.getBuyPrice();
                buyPrice = buyPrice / course.getCurrentCourse();
                product.setBuyPrice(buyPrice);
            }
        }
        return purchase;
    }*/
}
