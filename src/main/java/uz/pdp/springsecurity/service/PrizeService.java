package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.PrizeDto;
import uz.pdp.springsecurity.payload.PrizeGetDto;
import uz.pdp.springsecurity.repository.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PrizeService {
    private final BranchRepository branchRepository;
    private final SalaryRepository salaryRepository;
    private final BonusRepository bonusRepository;
    private final PrizeRepository prizeRepository;
    private final UserRepository userRepository;
    private final SalaryService salaryService;
    public ApiResponse add(PrizeDto prizeDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(prizeDto.getBranchId());
        if (optionalBranch.isEmpty()) return new ApiResponse("BRANCH NOT FOUND", false);
        Optional<Bonus> optionalBonus = bonusRepository.findByDeleteFalseAndId(prizeDto.getBonusId());
        if (optionalBonus.isEmpty()) return new ApiResponse("BONUS NOT FOUND", false);
        Optional<User> optionalUser = userRepository.findById(prizeDto.getUserId());
        if (optionalUser.isEmpty()) return new ApiResponse("USER NOT FOUND", false);
        Bonus bonus = optionalBonus.get();
        Prize prize = new Prize(
                            optionalBranch.get(),
                            optionalUser.get(),
                            bonus,
                            prizeDto.getDate(),
                            prizeDto.getDescription(),
                            prizeDto.isGiven()
                    );
        if (prizeDto.isGiven()){
            salaryService.add(optionalUser.get(), optionalBranch.get(), bonus.getSumma());
        }else if (prizeDto.isTask()){
            prize.setTask(true);
            prize.setDeadline(prizeDto.getDeadline());
            prize.setCount(prizeDto.getCount());
        }else if (prizeDto.isLid()){
            prize.setLid(true);
            prize.setDeadline(prizeDto.getDeadline());
            prize.setCount(prizeDto.getCount());
        }else {
            return new ApiResponse("BOOLEAN ERROR", false);
        }
        prizeRepository.save(prize);
        return new ApiResponse("SUCCESS", true);
    }

//    public ApiResponse edit(UUID prizeId, PrizeDto prizeDto) {
//        return new ApiResponse("SUCCESS", true);
//    }

    public ApiResponse getAll(UUID branchId) {
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NOT FOUND", false);
        List<Prize> prizeList = prizeRepository.findAllByBranchId(branchId);
        if (prizeList.isEmpty())return new ApiResponse("PRIZE NOT FOUND", false);
        return new ApiResponse(true, toPrizeGetDtoList(prizeList));
    }

    public ApiResponse getOne(UUID prizeId) {
        Optional<Prize> optionalPrize = prizeRepository.findById(prizeId);
        return optionalPrize.map(prize -> new ApiResponse(true, toPrizeGetDto(prize))).orElse(new ApiResponse("PRIZE NOT FOUND", false));
    }

    private PrizeGetDto toPrizeGetDto(Prize prize) {
        return new PrizeGetDto(
                prize.getId(),
                prize.getBranch().getId(),
                prize.getBranch().getName(),
                prize.getBonus().getId(),
                prize.getBonus().getName(),
                prize.getUser().getId(),
                prize.getUser().getFirstName(),
                prize.getUser().getLastName(),
                prize.getDate(),
                prize.getDescription(),
                prize.isGiven(),
                prize.isTask(),
                prize.isLid(),
                prize.getCount(),
                prize.getDeadline(),
                prize.getCounter()
        );
    }

    private List<PrizeGetDto> toPrizeGetDtoList(List<Prize> prizeList) {
        List<PrizeGetDto> prizeGetDtoList = new ArrayList<>();
        for (Prize prize : prizeList) {
            prizeGetDtoList.add(toPrizeGetDto(prize));
        }
        return prizeGetDtoList;
    }

    public ApiResponse getByUserAll(UUID userId, UUID branchId) {
        List<Prize> prizeList = prizeRepository.findAllByBranchIdAndUserIdOrderByDateDesc(branchId, userId);
        if (prizeList.isEmpty()) return new ApiResponse("PRIZE NOT FOUND", false);
        return new ApiResponse(true, toPrizeGetDtoList(prizeList));
    }

    public ApiResponse getByUserMonth(UUID userId, UUID branchId) {
        Optional<Salary> optionalSalary = salaryRepository.findByUserIdAndBranch_IdAndActiveTrue(userId, branchId);
        if (optionalSalary.isEmpty()) return new ApiResponse("ERROR", false);
        Salary salary = optionalSalary.get();
        List<Prize> prizeList = prizeRepository.findAllByBranchIdAndUserIdAndDateAfterAndGivenTrue(branchId, userId, salary.getStartDate());
        if (prizeList.isEmpty()) return new ApiResponse("PRIZE NOT FOUND", false);
        return new ApiResponse(true, toPrizeGetDtoList(prizeList));
    }
}