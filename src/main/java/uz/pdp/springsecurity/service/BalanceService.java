package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Balance;
import uz.pdp.springsecurity.entity.BalanceHistory;
import uz.pdp.springsecurity.entity.PaymentMethod;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.BalanceDto;
import uz.pdp.springsecurity.repository.BalanceHistoryRepository;
import uz.pdp.springsecurity.repository.BalanceRepository;
import uz.pdp.springsecurity.repository.PayMethodRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final BalanceRepository repository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final PayMethodRepository payMethodRepository;

    public ApiResponse edit(UUID branchId, Double summa, Boolean isPlus, List<UUID> payMethodId) {

        List<Balance> balanceList = repository.findAllByBranchId(branchId);


        if (payMethodId.isEmpty()) {
            return new ApiResponse("not found pay method", false);
        }
        if (balanceList.isEmpty()) {
            return new ApiResponse("not found Balance", false);
        }
        for (UUID paymentMethod : payMethodId) {
            Optional<Balance> optionalBalance = repository.findByPaymentMethod_Id(paymentMethod);
            if (optionalBalance.isPresent()) {
                Balance balance = optionalBalance.get();
                if (summa > 0) {
                    BalanceHistory newBalanceHistory = new BalanceHistory();
                    newBalanceHistory.setBalance(balance);
                    newBalanceHistory.setAccountSumma(balance.getAccountSumma());
                    newBalanceHistory.setTotalSumma(balance.getAccountSumma() + summa);

                    if (isPlus) {
                        balance.setAccountSumma(balance.getAccountSumma() + summa);
                    } else {
                        balance.setAccountSumma(balance.getAccountSumma() - summa);
                    }
                    newBalanceHistory.setPlus(isPlus);

                    balanceHistoryRepository.save(newBalanceHistory);
                    repository.save(balance);

                    return new ApiResponse("successfully saved", true);
                }
                break;
            }
        }
        return new ApiResponse("Must not be a number less than 0", false);
    }

    public ApiResponse getAll(UUID branchId) {
        List<Balance> balanceList = repository.findAllByBranchId(branchId);
        if (balanceList.isEmpty()) {
            return new ApiResponse("not found balance by branch id", false);
        }

        List<BalanceDto> balanceDtoList = new ArrayList<>();
        for (Balance balance : balanceList) {
            BalanceDto balanceDto = new BalanceDto();
            balanceDto.setBalanceSumma(balance.getAccountSumma());
            balanceDto.setBranchName(balance.getBranch().getName());
            balanceDto.setBranchId(balance.getBranch().getId());
            balanceDto.setPayMethodName(balance.getPaymentMethod().getType());
            balanceDto.setPaymentMethodId(balance.getPaymentMethod().getId());
            balanceDtoList.add(balanceDto);
        }

        return new ApiResponse("found", true, balanceDtoList);
    }
}
