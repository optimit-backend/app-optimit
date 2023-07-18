package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.HistoryName;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.OutlayDto;
import uz.pdp.springsecurity.repository.*;
import uz.pdp.springsecurity.utils.AppConstant;

import java.sql.Date;
import java.util.*;

@Service
public class OutlayService {
    @Autowired
    OutlayRepository outlayRepository;

    @Autowired
    OutlayCategoryRepository outlayCategoryRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    PayMethodRepository payMethodRepository;
    @Autowired
    BalanceService balanceService;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private HistoryRepository historyRepository;

    public ApiResponse add(OutlayDto outlayDto) {
        Outlay outlay = new Outlay();

        Optional<OutlayCategory> optionalCategory = outlayCategoryRepository.findById(outlayDto.getOutlayCategoryId());
        if (optionalCategory.isEmpty()) return new ApiResponse("OUTLAY CATEGORY NOT FOUND", false);
        outlay.setOutlayCategory(optionalCategory.get());

        outlay.setTotalSum(outlayDto.getTotalSum());

        Optional<Branch> optionalBranch = branchRepository.findById(outlayDto.getBranchId());
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("BRANCH NOT FOUND", false);
        }
        outlay.setBranch(optionalBranch.get());

        Optional<User> spender = userRepository.findById(outlayDto.getSpenderId());
        if (spender.isEmpty()) return new ApiResponse("SPENDER NOT FOUND", false);
        outlay.setSpender(spender.get());

        outlay.setDescription(outlayDto.getDescription());
        outlay.setDate(outlayDto.getDate());


        Optional<PaymentMethod> optionalPaymentMethod = payMethodRepository.findById(outlayDto.getPaymentMethodId());
        if (optionalPaymentMethod.isEmpty()) {
            return new ApiResponse("not found pay method id", false);
        }

        PaymentMethod paymentMethod = optionalPaymentMethod.get();
        outlay.setPaymentMethod(paymentMethod);

        balanceService.edit(optionalBranch.get().getId(), outlayDto.getTotalSum(), false, outlayDto.getPaymentMethodId());

        outlayRepository.save(outlay);
//        HISTORY
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        historyRepository.save(new History(
                HistoryName.XARAJAT,
                user,
                outlay.getBranch(),
                outlay.getTotalSum()+AppConstant.ADD_OUTLAY
        ));
        return new ApiResponse("ADDED", true);
    }

    public ApiResponse edit(UUID id, OutlayDto outlayDto) {
        if (!outlayRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);
        Optional<PaymentMethod> optionalPaymentMethod = payMethodRepository.findById(outlayDto.getPaymentMethodId());
        if (optionalPaymentMethod.isEmpty()) {
            return new ApiResponse("not found pay method id", false);
        }


        Outlay outlay = outlayRepository.getById(id);
        PaymentMethod paymentMethod = outlay.getPaymentMethod();
        double totalSum = outlay.getTotalSum();

        Optional<OutlayCategory> optionalCategory = outlayCategoryRepository.findById(outlayDto.getOutlayCategoryId());
        if (optionalCategory.isEmpty()) return new ApiResponse("OUTLAY CATEGORY NOT FOUND", false);
        outlay.setOutlayCategory(optionalCategory.get());

        outlay.setTotalSum(outlayDto.getTotalSum());

        Optional<Branch> optionalBranch = branchRepository.findById(outlayDto.getBranchId());
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("BRANCH NOT FOUND", false);
        }
        outlay.setBranch(optionalBranch.get());

        Optional<User> spender = userRepository.findById(outlayDto.getSpenderId());
        if (spender.isEmpty()) return new ApiResponse("SPENDER NOT FOUND", false);
        outlay.setSpender(spender.get());
        outlay.setDescription(outlayDto.getDescription());
        outlay.setDate(outlayDto.getDate());

        //eski summani balance ga qaytarish
        balanceService.edit(outlay.getBranch().getId(), totalSum, true, paymentMethod.getId());

        //yangi summa kiritish
        balanceService.edit(outlay.getBranch().getId(), outlayDto.getTotalSum(), false, outlayDto.getPaymentMethodId());

        outlayRepository.save(outlay);

//        HISTORY
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        historyRepository.save(new History(
                HistoryName.XARAJAT,
                user,
                outlay.getBranch(),
                AppConstant.EDIT_OUTLAY
        ));
        return new ApiResponse("EDITED", true);
    }

    public ApiResponse get(UUID id) {
        if (!outlayRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, outlayRepository.findById(id).get());
    }

    public ApiResponse delete(UUID id) {
        Optional<Outlay> optionalOutlay = outlayRepository.findById(id);
        if (optionalOutlay.isEmpty()) return new ApiResponse("NOT FOUND", false);
        Outlay outlay = optionalOutlay.get();
//        HISTORY
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        historyRepository.save(new History(
                HistoryName.XARAJAT,
                user,
                outlay.getBranch(),
                outlay.getTotalSum()+AppConstant.DELETE_OUTLAY
        ));
        outlayRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getByDate(Date date, UUID branch_id) {
        List<Outlay> allByDate = outlayRepository.findAllByDate(date, branch_id);
        if (allByDate.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByDate);
    }

    public ApiResponse getAllByBranchId(UUID branch_id) {
        List<Outlay> allByBranch_id = outlayRepository.findAllByBranch_Id(branch_id);
        if (allByBranch_id.isEmpty()) return new ApiResponse("NOT FOUND", false);
        allByBranch_id.sort(Comparator.comparing(Outlay::getTotalSum).reversed());
        return new ApiResponse("FOUND", true, allByBranch_id);
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        List<Outlay> allByBusinessId = outlayRepository.findAllByBranch_BusinessId(businessId);
        if (allByBusinessId.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }
        return new ApiResponse("FOUND", true, allByBusinessId);
    }

    public ApiResponse getAllByDate(Date date, UUID business_id) {
        List<Outlay> allByDateAndBusinessId = outlayRepository.findAllByDateAndBusinessId(business_id, date);
        if (allByDateAndBusinessId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByDateAndBusinessId);
    }
}
