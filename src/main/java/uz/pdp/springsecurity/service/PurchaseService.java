package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.entity.Currency;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;

    private final PurchaseProductRepository purchaseProductRepository;

    private final ProductRepository productRepository;

    private final SupplierRepository supplierRepository;

    private final ExchangeStatusRepository exchangeStatusRepository;

    private final PaymentStatusRepository paymentStatusRepository;

    private final BranchRepository branchRepository;

    private final UserRepository userRepository;

    private final CurrencyRepository currencyRepository;

    private final FifoCalculationService fifoCalculationService;

    private final ProductTypePriceRepository productTypePriceRepository;

    private final WarehouseService warehouseService;

    private final BalanceService balanceService;

    private final PayMethodRepository payMethodRepository;
    private final FifoCalculationRepository fifoCalculationRepository;


    public ApiResponse add(PurchaseDto purchaseDto) {
        Purchase purchase = new Purchase();
        return createOrEditPurchase(false, purchase, purchaseDto);
    }

    public ApiResponse edit(UUID id, PurchaseDto purchaseDto) {
        Optional<Purchase> optionalPurchase = purchaseRepository.findById(id);
        if (optionalPurchase.isEmpty()) return new ApiResponse("NOT FOUND", false);

        Purchase purchase = optionalPurchase.get();
        if (!purchase.isEditable()) return new ApiResponse("YOU CAN NOT EDIT AFTER 24 HOUR", false);
        LocalDateTime createdAt = purchase.getCreatedAt().toLocalDateTime();
        int day = LocalDateTime.now().getDayOfYear() - createdAt.getDayOfYear();
        if (day > 1) {
            purchase.setEditable(false);
            return new ApiResponse("YOU CAN NOT EDIT AFTER 24 HOUR", false);
        }
        return createOrEditPurchase(true, purchase, purchaseDto);
    }

    private ApiResponse createOrEditPurchase(boolean isEdit, Purchase purchase, PurchaseDto purchaseDto) {

        double oldSumma = 0;
        UUID payMethodId = null;
        if (isEdit) {
            if (purchase.getPaidSum() >= 0) {
                oldSumma = purchase.getPaidSum();
            }
            payMethodId = purchase.getPaymentMethod().getId();
        }

        if (purchaseDto.getPurchaseProductsDto().isEmpty()) {
            return new ApiResponse("NOT FOUND PURCHASE PRODUCT", false);
        }

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

        Optional<PaymentMethod> optionalPaymentMethod = payMethodRepository.findById(purchaseDto.getPaymentMethodId());
        if (optionalPaymentMethod.isEmpty()) {
            return new ApiResponse("NOT FOUND PAYMENT METHOD", false);
        }
        PaymentMethod paymentMethod = optionalPaymentMethod.get();
        purchase.setPaymentMethod(paymentMethod);

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

        UUID businessId = branch.getBusiness().getId();
        Optional<Currency> optionalCurrency = currencyRepository.findByBusinessId(businessId);
        double course = 11500;
        if (optionalCurrency.isPresent()){
            course = optionalCurrency.get().getCourse();
        }

        List<PurchaseProductDto> purchaseProductDtoList = purchaseDto.getPurchaseProductsDto();
        List<PurchaseProduct> purchaseProductList = new ArrayList<>();

        for (PurchaseProductDto purchaseProductDto : purchaseProductDtoList) {
            if (purchaseProductDto.getPurchaseProductId() == null) {
                PurchaseProduct purchaseProduct = createOrEditPurchaseProduct(new PurchaseProduct(), purchaseProductDto, course);
                if (purchaseProduct == null) continue;
                purchaseProduct.setPurchase(purchase);
                purchaseProductRepository.save(purchaseProduct);
                purchaseProductList.add(purchaseProduct);
                double minusAmount = warehouseService.createOrEditWareHouse(purchaseProduct, purchaseProduct.getPurchasedQuantity());
                fifoCalculationService.createPurchaseProduct(purchaseProduct, minusAmount);
            } else if (purchaseProductDto.isDelete()) {
                if (purchaseProductRepository.existsById(purchaseProductDto.getPurchaseProductId())) {
                    PurchaseProduct purchaseProduct = purchaseProductRepository.getById(purchaseProductDto.getPurchaseProductId());
                    warehouseService.createOrEditWareHouse(purchaseProduct, -purchaseProduct.getPurchasedQuantity());
                    purchaseProductRepository.deleteById(purchaseProductDto.getPurchaseProductId());
                }
            } else {
                Optional<PurchaseProduct> optionalPurchaseProduct = purchaseProductRepository.findById(purchaseProductDto.getPurchaseProductId());
                if (optionalPurchaseProduct.isEmpty()) continue;
                PurchaseProduct purchaseProduct = optionalPurchaseProduct.get();
                double amount = purchaseProductDto.getPurchasedQuantity() - purchaseProduct.getPurchasedQuantity();
                PurchaseProduct editPurchaseProduct = createOrEditPurchaseProduct(purchaseProduct, purchaseProductDto, course);
                if (editPurchaseProduct == null) continue;
                editPurchaseProduct.setPurchase(purchase);
                purchaseProductList.add(editPurchaseProduct);
                purchaseProduct.setPurchasedQuantity(purchaseProductDto.getPurchasedQuantity());
                fifoCalculationService.editPurchaseProduct(purchaseProduct, amount);
                warehouseService.createOrEditWareHouse(purchaseProduct, amount);
            }
        }
        purchaseProductRepository.saveAll(purchaseProductList);


        if (isEdit) {
            if (purchaseDto.getPaidSum() > 0) {
                balanceService.edit(branch.getId(), oldSumma, true, payMethodId);
            }
        }
        if (purchaseDto.getPaidSum() > 0) {
            balanceService.edit(branch.getId(), purchaseDto.getPaidSum(), false, payMethodId);
        }

        return new ApiResponse("SUCCESS", true);
    }

    private PurchaseProduct createOrEditPurchaseProduct(PurchaseProduct purchaseProduct, PurchaseProductDto purchaseProductDto, double course) {

        //SINGLE TYPE
        if (purchaseProductDto.getProductId() != null) {
            UUID productId = purchaseProductDto.getProductId();
            Optional<Product> optional = productRepository.findById(productId);
            if (optional.isEmpty()) return null;
            Product product = optional.get();
            product.setSalePrice(purchaseProductDto.getSalePrice());
            product.setBuyPrice(purchaseProductDto.getBuyPrice());
            product.setBuyPriceDollar(Math.round(purchaseProductDto.getBuyPrice() / course * 100) / 100.);
            product.setSalePriceDollar(Math.round(purchaseProductDto.getSalePrice() / course * 100) / 100.);
            productRepository.save(product);
            purchaseProduct.setProduct(product);
        } else {//MANY TYPE
            UUID productTypePriceId = purchaseProductDto.getProductTypePriceId();
            Optional<ProductTypePrice> optional = productTypePriceRepository.findById(productTypePriceId);
            if (optional.isEmpty()) return null;
            ProductTypePrice productTypePrice = optional.get();
            productTypePrice.setBuyPrice(purchaseProductDto.getBuyPrice());
            productTypePrice.setSalePrice(purchaseProductDto.getSalePrice());
            productTypePrice.setBuyPriceDollar(Math.round(purchaseProductDto.getBuyPrice() / course * 100) / 100.);
            productTypePrice.setSalePriceDollar(Math.round(purchaseProductDto.getSalePrice() / course * 100) / 100.);
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
    }

    public ApiResponse getOne(UUID id) {
        Optional<Purchase> optionalPurchase = purchaseRepository.findById(id);
        if (optionalPurchase.isEmpty()) return new ApiResponse("NOT FOUND PURCHASE", false);
        Purchase purchase = optionalPurchase.get();
        List<PurchaseProduct> purchaseProductList = purchaseProductRepository.findAllByPurchaseId(purchase.getId());
        if (purchaseProductList.isEmpty()) return new ApiResponse("NOT FOUND PRODUCTS", false);
        PurchaseGetOneDto purchaseGetOneDto = new PurchaseGetOneDto(
                purchase,
                purchaseProductList
        );
        return new ApiResponse("FOUND", true, purchaseGetOneDto);
    }

    public ApiResponse view(UUID purchaseId) {
        Optional<Purchase> optionalPurchase = purchaseRepository.findById(purchaseId);
        if (optionalPurchase.isEmpty()) return new ApiResponse("NOT FOUND PURCHASE", false);
        Purchase purchase = optionalPurchase.get();
        List<PurchaseProduct> purchaseProductList = purchaseProductRepository.findAllByPurchaseId(purchase.getId());
        if (purchaseProductList.isEmpty()) return new ApiResponse("NOT FOUND PRODUCTS", false);
        Map<String, Object> response = new HashMap<>();
        response.put("purchase", purchase);
        response.put("purchaseProductGetDtoList", toPurchaseProductGetDtoList(purchaseProductList));
        return new ApiResponse("FOUND", true, response);
    }

    private List<PurchaseProductGetDto> toPurchaseProductGetDtoList(List<PurchaseProduct> purchaseProductList) {
        List<PurchaseProductGetDto> purchaseProductGetDtoList = new ArrayList<>();
        for (PurchaseProduct purchaseProduct : purchaseProductList) {
            PurchaseProductGetDto dto = new PurchaseProductGetDto(
                    purchaseProduct.getPurchasedQuantity(),
                    purchaseProduct.getBuyPrice(),
                    purchaseProduct.getSalePrice(),
                    purchaseProduct.getTotalSum()
            );
            if (purchaseProduct.getProduct() != null) {
                dto.setName(purchaseProduct.getProduct().getName());
                dto.setMeasurement(purchaseProduct.getProduct().getMeasurement().getName());
            } else {
                dto.setName(purchaseProduct.getProductTypePrice().getName());
                dto.setMeasurement(purchaseProduct.getProductTypePrice().getProduct().getMeasurement().getName());
            }
            double remainQuantity= fifoCalculationRepository.remainQuantityByPurchaseProductId(purchaseProduct.getId());
            remainQuantity = Math.round(remainQuantity * 100) / 100.;
            dto.setSoldQuantity(dto.getQuantity() - remainQuantity);
            dto.setProfit((dto.getSalePrice() - dto.getBuyPrice()) * dto.getSoldQuantity());
            purchaseProductGetDtoList.add(dto);
        }
        return purchaseProductGetDtoList;
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
    }

    public ApiResponse getByPurchaseStatusId(UUID purchaseStatus_id) {
        List<Purchase> allByPurchaseStatus_id = purchaseRepository.findAllByPurchaseStatus_Id(purchaseStatus_id);
        if (allByPurchaseStatus_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByPurchaseStatus_id);
    }

    public ApiResponse getByPaymentStatusId(UUID paymentStatus_id) {
        List<Purchase> allByPaymentStatus_id = purchaseRepository.findAllByPaymentStatus_Id(paymentStatus_id);
        if (allByPaymentStatus_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByPaymentStatus_id);
    }

    public ApiResponse getByBranchId(UUID branch_id) {
        List<Purchase> allByBranch_id = purchaseRepository.findAllByBranch_Id(branch_id);
        if (allByBranch_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByBranch_id);
    }
}
