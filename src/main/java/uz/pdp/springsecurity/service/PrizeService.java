package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.PrizeDto;
import uz.pdp.springsecurity.payload.PrizeGetDto;
import uz.pdp.springsecurity.payload.PrizeProjectTaskDto;
import uz.pdp.springsecurity.repository.*;

import javax.validation.Valid;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PrizeService {
    private final ProjectRepository projectRepository;
    private final BranchRepository branchRepository;
    private final SalaryRepository salaryRepository;
    private final BonusRepository bonusRepository;
    private final PrizeRepository prizeRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SalaryService salaryService;
    public ApiResponse add(PrizeDto prizeDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(prizeDto.getBranchId());
        if (optionalBranch.isEmpty()) return new ApiResponse("BRANCH NOT FOUND", false);
        Optional<Bonus> optionalBonus = bonusRepository.findByDeleteFalseAndId(prizeDto.getBonusId());
        if (optionalBonus.isEmpty()) return new ApiResponse("BONUS NOT FOUND", false);
        if (prizeDto.getUserIdSet().isEmpty()) return new ApiResponse("USER SET NOT FOUND", false);
        Branch branch = optionalBranch.get();
        Bonus bonus = optionalBonus.get();
        Set<User> userSet = new HashSet<>();
        for (UUID userId : prizeDto.getUserIdSet()) {
            Optional<User> optionalUser = userRepository.findByIdAndBranchesIdAndActiveIsTrue(userId, branch.getId());
            if (optionalUser.isEmpty())  return new ApiResponse("USER NOT FOUND", false, userId);
            userSet.add(optionalUser.get());
        }
        if (userSet.isEmpty()) return new ApiResponse("USER SET NOT FOUND", false);
        prizeRepository.save(new Prize(
                branch,
                userSet,
                bonus,
                prizeDto.getDate(),
                prizeDto.getDescription(),
                true
        ));
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse addForProject(@Valid PrizeProjectTaskDto prizeDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(prizeDto.getBranchId());
        if (optionalBranch.isEmpty()) return new ApiResponse("BRANCH NOT FOUND", false);
        Optional<Bonus> optionalBonus = bonusRepository.findByDeleteFalseAndId(prizeDto.getBonusId());
        if (optionalBonus.isEmpty()) return new ApiResponse("BONUS NOT FOUND", false);
        Optional<Project> optionalProject = projectRepository.findById(prizeDto.getProjectOrTaskId());
        if (optionalProject.isEmpty()) return new ApiResponse("PROJECT NOT FOUND", false);
        if (prizeDto.getUserIdSet().isEmpty()) return new ApiResponse("USER SET NOT FOUND", false);
        Branch branch = optionalBranch.get();
        Set<User> userSet = new HashSet<>();
        for (UUID userId : prizeDto.getUserIdSet()) {
            Optional<User> optionalUser = userRepository.findByIdAndBranchesIdAndActiveIsTrue(userId, branch.getId());
            if (optionalUser.isEmpty())  return new ApiResponse("USER NOT FOUND", false, userId);
            userSet.add(optionalUser.get());
        }
        if (userSet.isEmpty()) return new ApiResponse("USER SET NOT FOUND", false);
        prizeRepository.save(new Prize(
                branch,
                userSet,
                optionalBonus.get(),
                optionalProject.get(),
                prizeDto.getDeadline(),
                prizeDto.getDate(),
                prizeDto.getDescription(),
                prizeDto.isGiven()
        ));
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse addForTask(@Valid PrizeProjectTaskDto prizeDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(prizeDto.getBranchId());
        if (optionalBranch.isEmpty()) return new ApiResponse("BRANCH NOT FOUND", false);
        Optional<Bonus> optionalBonus = bonusRepository.findByDeleteFalseAndId(prizeDto.getBonusId());
        if (optionalBonus.isEmpty()) return new ApiResponse("BONUS NOT FOUND", false);
        Optional<Task> optionalTask = taskRepository.findById(prizeDto.getProjectOrTaskId());
        if (optionalTask.isEmpty()) return new ApiResponse("PROJECT NOT FOUND", false);
        if (prizeDto.getUserIdSet().isEmpty()) return new ApiResponse("USER SET NOT FOUND", false);
        Branch branch = optionalBranch.get();
        Set<User> userSet = new HashSet<>();
        for (UUID userId : prizeDto.getUserIdSet()) {
            Optional<User> optionalUser = userRepository.findByIdAndBranchesIdAndActiveIsTrue(userId, branch.getId());
            if (optionalUser.isEmpty())  return new ApiResponse("BONUS NOT FOUND", false, userId);
            userSet.add(optionalUser.get());
        }
        if (userSet.isEmpty()) return new ApiResponse("USER SET NOT FOUND", false);
        Prize prize = prizeRepository.save(new Prize(
                branch,
                userSet,
                optionalBonus.get(),
                optionalTask.get(),
                prizeDto.getDeadline(),
                prizeDto.getDate(),
                prizeDto.getDescription(),
                prizeDto.isGiven()
        ));
        if (prize.isGiven())addToSalary(userSet, branch, prize.getBonus().getSumma());
        return new ApiResponse("SUCCESS", true);
    }

    private void addToSalary(Set<User> userSet, Branch branch, Double salarySum) {
        for (User user : userSet) {
            salaryService.add(user, branch, salarySum);
        }
    }


    /*public ApiResponse edit(UUID prizeId, PrizeDto prizeDto) {
        return new ApiResponse("SUCCESS", true);
    }*/

    public ApiResponse getAll(UUID branchId) {
        if (branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NOT FOUND", false);
        List<Prize> prizeList = prizeRepository.findAllByBranchId(branchId);
        if (prizeList.isEmpty())return new ApiResponse("PRIZE NOT FOUND", false);
        return new ApiResponse(true, toPrizeGetDtoList(prizeList));
    }

    public ApiResponse getOne(UUID prizeId) {
        Optional<Prize> optionalPrize = prizeRepository.findById(prizeId);
        return optionalPrize.map(prize -> new ApiResponse(true, toPrizeGetDto(prize))).orElse(new ApiResponse("PRIZE NOT FOUND", false));
    }

    private PrizeGetDto toPrizeGetDto(Prize prize) {
        Set<UUID> userIdSet = new HashSet<>();
        for (User user : prize.getUserSet()) {
            userIdSet.add(user.getId());
        }
        PrizeGetDto prizeGetDto = new PrizeGetDto(
                prize.getId(),
                prize.getBranch().getId(),
                prize.getBranch().getName(),
                prize.getBonus().getId(),
                prize.getBonus().getName(),
                userIdSet,
                prize.getDate(),
                prize.getDescription(),
                prize.isGiven(),
                prize.getDeadline()
        );
        if (prize.getProject() != null) {
            prizeGetDto.setProjectId(prize.getProject().getId());
            prizeGetDto.setProjectName(prize.getProject().getName());
        } else if (prize.getTask() != null) {
            prizeGetDto.setTaskId(prize.getTask().getId());
            prizeGetDto.setTaskName(prize.getTask().getName());
        }
        return prizeGetDto;
    }

    private List<PrizeGetDto> toPrizeGetDtoList(List<Prize> prizeList) {
        List<PrizeGetDto> prizeGetDtoList = new ArrayList<>();
        for (Prize prize : prizeList) {
            prizeGetDtoList.add(toPrizeGetDto(prize));
        }
        return prizeGetDtoList;
    }

    public ApiResponse getByUserAll(UUID userId, UUID branchId) {
        List<Prize> prizeList = prizeRepository.findAllByBranchIdAndUserSetIdOrderByDateDesc(branchId, userId);
        if (prizeList.isEmpty()) return new ApiResponse("PRIZE NOT FOUND", false);
        return new ApiResponse(true, toPrizeGetDtoList(prizeList));
    }

    public ApiResponse getByUserMonth(UUID userId, UUID branchId) {
        Optional<Salary> optionalSalary = salaryRepository.findByUserIdAndBranch_IdAndActiveTrue(userId, branchId);
        if (optionalSalary.isEmpty()) return new ApiResponse("ERROR", false);
        Salary salary = optionalSalary.get();
        List<Prize> prizeList = prizeRepository.findAllByBranchIdAndUserSetIdAndDateAfterAndGivenTrue(branchId, userId, salary.getStartDate());
        if (prizeList.isEmpty()) return new ApiResponse("PRIZE NOT FOUND", false);
        return new ApiResponse(true, toPrizeGetDtoList(prizeList));
    }
}
