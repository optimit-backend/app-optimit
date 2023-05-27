package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProductHistoryGetDto;
import uz.pdp.springsecurity.repository.BranchRepository;
import uz.pdp.springsecurity.repository.ProductHistoryRepository;
import uz.pdp.springsecurity.repository.WarehouseRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductHistoryService {
    private final ProductHistoryRepository productHistoryRepository;
    private final BranchRepository branchRepository;
    private final WarehouseRepository warehouseRepository;
    LocalDateTime TODAY_START = LocalDate.now().atStartOfDay();
    public void create(Branch branch, Product product, ProductTypePrice productTypePrice, boolean plus, double plusAmount, double amount) {
        Optional<ProductHistory> optionalProductHistory;
        if (product != null)
            optionalProductHistory = productHistoryRepository.findByBranchIdAndProductIdAndCreatedAtBetween(branch.getId(), product.getId(), Timestamp.valueOf(TODAY_START), Timestamp.valueOf(TODAY_START.plusDays(1)));
        else
            optionalProductHistory = productHistoryRepository.findByBranchIdAndProductTypePriceIdAndCreatedAtBetween(branch.getId(), productTypePrice.getId(), Timestamp.valueOf(TODAY_START), Timestamp.valueOf(TODAY_START.plusDays(1)));
        if (optionalProductHistory.isPresent()) {
            edit(optionalProductHistory.get(), plus, plusAmount, amount);
        } else {
            if (productHistoryRepository.existsAllByCreatedAtBetween(Timestamp.valueOf(TODAY_START), Timestamp.valueOf(TODAY_START.plusDays(1)))) {
                productHistoryRepository.save(new ProductHistory(
                        product,
                        productTypePrice,
                        branch,
                        amount,
                        0,
                        0
                ));
            } else {
                createAll(branch);
            }
            create(branch, product, productTypePrice, plus, plusAmount, amount);
        }
    }

    private void edit(ProductHistory history, boolean plus, double plusAmount, double amount) {
        if (plus)
            history.setPlusAmount(history.getPlusAmount() + plusAmount);
        else
            history.setMinusAmount(history.getMinusAmount() + plusAmount);
        history.setAmount(amount);
        productHistoryRepository.save(history);
    }

    private boolean createAll(Branch branch) {
        List<Warehouse> warehouseList = warehouseRepository.findAllByBranchId(branch.getId());
        for (Warehouse warehouse : warehouseList) {
            productHistoryRepository.save(new ProductHistory(
                    warehouse.getProduct(),
                    warehouse.getProductTypePrice(),
                    branch,
                    warehouse.getAmount(),
                    0,
                    0
            ));
        }
        return !warehouseList.isEmpty();
    }

    public ApiResponse get(UUID branchId, Date date, int page, int size) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty())
            return new ApiResponse("BRANCH NOT FOUND", false);
        Pageable pageable = PageRequest.of(page, size);
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDateTime from = LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), 0, 0, 0);
        Page<ProductHistory> historyPage = productHistoryRepository.findAllByBranchIdAndCreatedAtBetween(branchId, Timestamp.valueOf(from), Timestamp.valueOf(from.plusDays(1)), pageable);
        List<ProductHistoryGetDto> dtoList = new ArrayList<>();
        for (ProductHistory history : historyPage.getContent()) {
            ProductHistoryGetDto dto = new ProductHistoryGetDto();
            if (history.getProduct() != null) {
                dto.setName(history.getProduct().getName());
                dto.setBarcode(history.getProduct().getBarcode());
                dto.setMeasurement(history.getProduct().getMeasurement().getName());
            } else {
                dto.setName(history.getProductTypePrice().getName());
                dto.setBarcode(history.getProductTypePrice().getBarcode());
                dto.setMeasurement(history.getProductTypePrice().getProduct().getMeasurement().getName());
            }
            dto.setDate(history.getCreatedAt());
            dto.setAmount(history.getAmount());
            dto.setPlusAmount(history.getPlusAmount());
            dto.setMinusAmount(history.getMinusAmount());
            dtoList.add(dto);
        }
        if (dtoList.isEmpty()){
            if (localDate.getDayOfYear() == TODAY_START.getDayOfYear()) {
                boolean check = createAll(optionalBranch.get());
                if (check)
                    return get(branchId, date, page, size);
            }
            return new ApiResponse("MA'LUMOT TOPILMADI", false);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("dtoList", dtoList);
        response.put("currentPage", historyPage.getNumber());
        response.put("totalItem", historyPage.getTotalElements());
        response.put("totalPage", historyPage.getTotalPages());
        return new ApiResponse("SUCCESS", true, response);
    }
}
