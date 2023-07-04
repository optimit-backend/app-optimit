package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;
import uz.pdp.springsecurity.utils.ConstantProduct;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LossService {
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductTypePriceRepository productTypePriceRepository;
    private final LossRepository lossRepository;
    private final LossProductRepository lossProductRepository;
    private final WarehouseService warehouseService;
    private final FifoCalculationService fifoCalculationService;
    private final ProductAboutRepository productAboutRepository;

    public ApiResponse create(LossDTO lossDTO) {
        Optional<Branch> optionalBranch = branchRepository.findById(lossDTO.getBranchId());
        if (optionalBranch.isEmpty())
            return new ApiResponse("BRANCH NOT FOUND", false);
        Optional<User> optionalUser = userRepository.findById(lossDTO.getUserId());
        if (optionalUser.isEmpty())
            return new ApiResponse("USER NOT FOUND", false);
        Loss loss = new Loss(
                optionalUser.get(),
                optionalBranch.get(),
                lossDTO.getDate()
        );

        List<LossProduct> lossProductList = new ArrayList<>();
        ApiResponse apiResponse = toLossProductList(loss, lossProductList, lossDTO.getLossProductDtoList());
        if (!apiResponse.isSuccess())
            return apiResponse;
        lossRepository.save(loss);
        lossProductRepository.saveAll(lossProductList);
        return new ApiResponse("SUCCESS", true);
    }

    private ApiResponse toLossProductList(Loss loss, List<LossProduct> lossProductList, List<LossProductDto> lossProductDtoList) {
        for (LossProductDto dto : lossProductDtoList) {
            LossProduct lossProduct = new LossProduct(
                    loss,
                    dto.getQuantity()
            );
            if (dto.getProductId() != null) {
                Optional<Product> optionalProduct = productRepository.findById(dto.getProductId());
                if (optionalProduct.isEmpty())
                    return new ApiResponse("PRODUCT NOT FOUND", false);
                lossProduct.setProduct(optionalProduct.get());
            } else {
                Optional<ProductTypePrice> optionalProductTypePrice = productTypePriceRepository.findById(dto.getProductTypePriceId());
                if (optionalProductTypePrice.isEmpty())
                    return new ApiResponse("PRODUCT NOT FOUND", false);
                lossProduct.setProductTypePrice(optionalProductTypePrice.get());
            }
            warehouseService.createOrEditWareHouseHelper(loss.getBranch(), lossProduct.getProduct(), lossProduct.getProductTypePrice(), -dto.getQuantity());
            fifoCalculationService.createLossProduct(loss.getBranch(), lossProduct);
            lossProductList.add(lossProduct);
            // save product history
            productAboutRepository.save(new ProductAbout(
                    lossProduct.getProduct(),
                    lossProduct.getProductTypePrice(),
                    loss.getBranch(),
                    ConstantProduct.LOSE,
                    -lossProduct.getQuantity()
            ));
        }
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse get(UUID branchId, int page, int size) {
        if (!branchRepository.existsById(branchId))
            return new ApiResponse("BRANCH NOT FOUND", false);
        Pageable pageable = PageRequest.of(page, size);
        Page<Loss> lossPage = lossRepository.findAllByBranchIdOrderByCreatedAtDesc(branchId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("list", toLossGetDtoList(lossPage.getContent()));
        response.put("currentPage", lossPage.getNumber());
        response.put("totalItems", lossPage.getTotalElements());
        response.put("totalPages", lossPage.getTotalPages());
        return new ApiResponse("SUCCESS", true, response);
    }

    private List<LossGetDto> toLossGetDtoList(List<Loss> lossList) {
        List<LossGetDto> list = new ArrayList<>();
        for (Loss loss : lossList) {
            list.add(new LossGetDto(
                    loss.getId(),
                    loss.getUser().getFirstName() + " " + loss.getUser().getLastName(),
                    loss.getDate()
            ));
        }
        return list;
    }

    public ApiResponse getOne(UUID lossId) {
        if (!lossRepository.existsById(lossId))
            return new ApiResponse("LOSS NOT FOUND", false);
        List<LossProduct> lossProductList = lossProductRepository.findAllByLossId(lossId);
        List<LossProductGetDto> list = new ArrayList<>();
        for (LossProduct l : lossProductList) {
            LossProductGetDto dto = new LossProductGetDto();
            if (l.getProduct() != null) {
                dto.setName(l.getProduct().getName());
                dto.setMeasurement(l.getProduct().getMeasurement().getName());
            } else {
                dto.setName(l.getProductTypePrice().getName());
                dto.setMeasurement(l.getProductTypePrice().getProduct().getMeasurement().getName());
            }
            dto.setQuantity(l.getQuantity());
            list.add(dto);
        }
        return new ApiResponse("SUCCESS", true, list);
    }

}
