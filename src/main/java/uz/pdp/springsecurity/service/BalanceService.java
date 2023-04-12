package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Balance;
import uz.pdp.springsecurity.entity.BalanceHistory;
import uz.pdp.springsecurity.entity.Branch;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.BalanceDto;
import uz.pdp.springsecurity.repository.BalanceHistoryRepository;
import uz.pdp.springsecurity.repository.BalanceRepository;
import uz.pdp.springsecurity.repository.BranchRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final BalanceRepository repository;
    private final BranchRepository branchRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    public ApiResponse add(UUID branchId) {
        Balance balance = repository.findByBranchId(branchId).orElse(null);
        Branch branch = branchRepository.findById(branchId).orElse(null);
        if (branch == null) {
            return new ApiResponse("Branch id must not be null", false);
        }
        if (balance != null) {
            return new ApiResponse("Balance impossible to create", false);
        }
        Balance newBalance = new Balance();
        newBalance.setBranch(branch);
        newBalance.setAccountSumma(0);
        repository.save(newBalance);

        return new ApiResponse("successfully saved", true);
    }

    public ApiResponse edit(UUID branchId, Double summa) {

        Optional<Balance> optionalBalance = repository.findByBranchId(branchId);
        Branch branch = branchRepository.findById(branchId).orElse(null);

        if (optionalBalance.isEmpty()) {
            return new ApiResponse("not found Balance", false);
        }

        Balance balance = optionalBalance.get();

        if (summa > 0) {

            BalanceHistory newBalanceHistory = new BalanceHistory();
            newBalanceHistory.setPlus(true);
            newBalanceHistory.setBranch(branch);
            newBalanceHistory.setAccountSumma(balance.getAccountSumma());
            newBalanceHistory.setTotalSumma(balance.getAccountSumma() + summa);

            balance.setAccountSumma(balance.getAccountSumma() + summa);

            balanceHistoryRepository.save(newBalanceHistory);
            repository.save(balance);

            return new ApiResponse("successfully saved", true);
        }
        return new ApiResponse("Must not be a number less than 0", false);
    }

    public ApiResponse getAll(UUID branchId) {
        Optional<Balance> optionalBalance = repository.findByBranchId(branchId);
        if (optionalBalance.isEmpty()) {
            return new ApiResponse("not found balance by branch id", false);
        }

        Balance balance = optionalBalance.get();
        BalanceDto balanceDto = new BalanceDto();
        return null;
    }
}
