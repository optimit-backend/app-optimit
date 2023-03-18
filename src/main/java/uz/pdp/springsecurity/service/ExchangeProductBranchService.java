package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ExchangeProductBranchDTO;
import uz.pdp.springsecurity.payload.ExchangeProductDTO;
import uz.pdp.springsecurity.repository.*;

import java.sql.Date;
import java.util.*;

@Service
public class ExchangeProductBranchService {

    @Autowired
    ExchangeProductBranchRepository exchangeProductBranchRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    ExchangeStatusRepository exchangeStatusRepository;

    @Autowired
    ExchangeProductRepository exchangeProductRepository;

//    public ApiResponse create(ExchangeProductBranchDTO exchangeProductBranchDTO) {
//        ExchangeProductBranch exchangeProductBranch = new ExchangeProductBranch();
//        return add(exchangeProductBranch, exchangeProductBranchDTO);
//    }
//
//
//    public ApiResponse edit(UUID id, ExchangeProductBranchDTO exchangeProductBranchDTO) {
//        Optional<ExchangeProductBranch> optionalExchange = exchangeProductBranchRepository.findById(id);
//        if (optionalExchange.isEmpty()) return new ApiResponse("NOT FOUND", false);
//        ExchangeProductBranch exchange = optionalExchange.get();
//        return add(exchange, exchangeProductBranchDTO);
//    }


//    public ApiResponse add(ExchangeProductBranch exchangeProductBranch, ExchangeProductBranchDTO exchangeProductBranchDTO) {
//        if ((exchangeProductBranchDTO.getShippedBranchId() == exchangeProductBranchDTO.getReceivedBranchId())) {
//            return new ApiResponse("SELECT ANOTHER BRANCH TO SEND", false);
//        }
//
//        /*
//         * JO'NATUVCHI FILIALNI SAQLASH
//         */
//        UUID shippedBranchId = exchangeProductBranchDTO.getShippedBranchId();
//        Optional<Branch> shippedBranchOptional = branchRepository.findById(shippedBranchId);
//        if (shippedBranchOptional.isEmpty()) {
//            return new ApiResponse("SHIPPED BRANCH NOT FOUND", false);
//        }
//        exchangeProductBranch.setShippedBranch(shippedBranchOptional.get());
//
//
//        UUID receivedBranch = exchangeProductBranchDTO.getReceivedBranchId();
//        Optional<Branch> branchOptional = branchRepository.findById(receivedBranch);
//        if (branchOptional.isEmpty()) {
//            return new ApiResponse("RECEIVED BRANCH NOT FOUND", false);
//        }
//        exchangeProductBranch.setReceivedBranch(branchOptional.get());
//
//        /*
//         * DATENI SAQLSH
//         */
//        if (exchangeProductBranchDTO.getExchangeDate() == null) {
//            Date date = new Date(System.currentTimeMillis());
//            exchangeProductBranch.setExchangeDate(date);
//        } else {
//            exchangeProductBranch.setExchangeDate(exchangeProductBranchDTO.getExchangeDate());
//        }
//
//        /*
//         * DESCRIPTIONI SAQLSH
//         */
//        exchangeProductBranch.setDescription(exchangeProductBranchDTO.getDescription());
//
//        /*
//         * JO'NATILGAN PRODUCTNI SAQLASH
//         */
//
//        List<ExchangeProductDTO> exchangeProductDTOS = exchangeProductBranchDTO.getExchangeProductDTOS();
//        List<ExchangeProduct> exchangeProductList = new ArrayList<>();
//        for (ExchangeProductDTO productDTO : exchangeProductDTOS) {
//            Product product = new Product();
//            productRepository.findByBarcodeAndBranchIdAndActive(product.getBarcode(), productDTO.getProductExchangeId(), product.isActive() );
//            Optional<Product> exchangeProduct = productRepository.findByIdAndBranch_IdAndActiveTrue(productDTO.getProductExchangeId(), shippedBranchOptional.get().getId());
//            if (exchangeProduct.isPresent()) {
//                if ((exchangeProduct.get().getQuantity() < productDTO.getExchangeProductQuantity())) {
//                    return new ApiResponse("THE QUANTITY OF PRODUCT IS NOT ENOUGH TO TRANSFER!", false);
//                }
//
//                /*
//                 * EXCHANGE BO'LGAN PRODUCT SAQLANYABDI
//                 */
//                ExchangeProduct exchangeProduct1 = new ExchangeProduct(productDTO.getExchangeProductQuantity(), exchangeProduct.get());
//                exchangeProductList.add(exchangeProduct1);
//                exchangeProductRepository.save(exchangeProduct1);
//
//                product = exchangeProduct.get();
//                product.setQuantity(product.getQuantity() - productDTO.getExchangeProductQuantity());
//                productRepository.save(product);
//            } else {
//                return new ApiResponse("PRODUCT NOT FOUND");
//            }
//
//
//            Optional<Product> optionalProduct = productRepository.findByBarcodeAndBranch_IdAndActiveTrue(product.getBarcode(), receivedBranch);
//            if (optionalProduct.isPresent()) {
//                Product receiveProduct = optionalProduct.get();
//                receiveProduct.setQuantity(receiveProduct.getQuantity() + productDTO.getExchangeProductQuantity());
//                productRepository.save(receiveProduct);
//            } else {
//                Product newProduct = new Product();
//                newProduct.setName(product.getName());
//                newProduct.setBrand(product.getBrand());
//                newProduct.setBarcode(product.getBarcode());
//                newProduct.setCategory(product.getCategory());
//                newProduct.setMeasurement(product.getMeasurement());
//                newProduct.setMinQuantity(product.getMinQuantity());
////                newProduct.setPhoto(product.getPhoto());                  xato beryapti iltimos ochma!!!
//                newProduct.setBuyPrice(product.getBuyPrice());
//                newProduct.setSalePrice(product.getSalePrice());
//                newProduct.setTax(product.getTax());
//                newProduct.setExpireDate(product.getExpireDate());
//                newProduct.setQuantity(productDTO.getExchangeProductQuantity());
//
//
//
//                productRepository.save(newProduct);
//            }
//        }
//
//        exchangeProductBranch.setExchangeProduct(exchangeProductList);
//
//
//        Optional<ExchangeStatus> optionalExchangeStatus = exchangeStatusRepository.findById(exchangeProductBranchDTO.getExchangeStatusId());
//        if (optionalExchangeStatus.isEmpty()) return new ApiResponse("STATUS NOT FOUND",false);
//        exchangeProductBranch.setExchangeStatus(optionalExchangeStatus.get());
//
//        Optional<Business> optionalBusiness = businessRepository.findById(exchangeProductBranchDTO.getBusinessId());
//        if (optionalBusiness.isEmpty()) return new ApiResponse("BUSINESS NOT FOUND",false);
//
//
//        exchangeProductBranch.setBusiness(optionalBusiness.get());
//
//        exchangeProductBranchRepository.save(exchangeProductBranch);
//
//        return new ApiResponse("SENT", true);
//    }

    public ApiResponse getOne(UUID id) {
        if (exchangeProductBranchRepository.findById(id).isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, exchangeProductBranchRepository.getById(id));
    }

    public ApiResponse deleteOne(UUID id) {
        if (exchangeProductBranchRepository.findById(id).isEmpty()) return new ApiResponse("NOT FOUND", false);
        exchangeProductBranchRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getByDate(Date exchangeDate, UUID business_id) {
        List<ExchangeProductBranch> allByExchangeDate = exchangeProductBranchRepository.findAllByExchangeDateAndBusiness_Id(exchangeDate, business_id);
        return new ApiResponse("FOUND", true, allByExchangeDate);
    }

    public ApiResponse getByStatusId(UUID exchangeStatusId, UUID branch_id) {
        List<ExchangeProductBranch> allByExchangeStatus_id = exchangeProductBranchRepository.findAllByExchangeStatus_IdAndBusiness_Id(exchangeStatusId, branch_id);
        if (allByExchangeStatus_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByExchangeStatus_id);
    }

    public ApiResponse getByBusinessId(UUID businessId) {
        List<ExchangeProductBranch> allByBusinessId = exchangeProductBranchRepository.findAllByBusiness_Id(businessId);
        if (allByBusinessId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByBusinessId);
    }

    public ApiResponse getByShippedBranchId(UUID shippedBranch_id) {
        List<ExchangeProductBranch> allByShippedBranch_id = exchangeProductBranchRepository.findAllByShippedBranch_Id(shippedBranch_id);
        if (allByShippedBranch_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByShippedBranch_id);
    }

    public ApiResponse getByReceivedBranchId(UUID receivedBranch_id) {
        List<ExchangeProductBranch> allByShippedBranch_id = exchangeProductBranchRepository.findAllByReceivedBranch_Id(receivedBranch_id);
        if (allByShippedBranch_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByShippedBranch_id);
    }
}
