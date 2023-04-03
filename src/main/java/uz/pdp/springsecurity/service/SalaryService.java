package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.springsecurity.entity.Branch;
import uz.pdp.springsecurity.entity.Salary;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.mapper.SalaryMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.SalaryDto;
import uz.pdp.springsecurity.repository.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalaryService {
    private final SalaryRepository salaryRepository;
    private final SalaryCountRepository salaryCountRepository;
    private final WorkTimeRepository workTimeRepository;
    private final SalaryMapper salaryMapper;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    public void add(User user, Branch branch, double salarySum) {
        Optional<Salary> optionalSalary = salaryRepository.findByUserIdAndBranch_IdAndActiveTrue(user.getId(), branch.getId());
        if (optionalSalary.isEmpty()) {
            Date date = new Date();
            salaryRepository.save(new Salary(
                    user,
                    branch,
                    0d,
                    salarySum,
                    0d,
                    true,
                    date,
                    date
            ));
            return;
        }
        Salary salary = optionalSalary.get();
        salary.setSalary(salary.getSalary() + salarySum);
        salaryRepository.save(salary);
    }



    @Transactional
    public ApiResponse paySalary(UUID salaryId, SalaryDto salaryDto) {
        Optional<Salary> optionalSalary = salaryRepository.findByIdAndActiveTrue(salaryId);
        if (optionalSalary.isEmpty()) return new ApiResponse("SALARY NOT FOUND", false);
        Salary salary = optionalSalary.get();
        double totalSalary = salary.getRemain() + salary.getSalary();

        User user = salary.getUser();
        Branch branch = salary.getBranch();
        Date now = new Date();
        salary.setPayedSum(salary.getPayedSum() + salaryDto.getSalary());
        salary.setDescription(salaryDto.getDescription());
        salary.setEndDate(now);
        salary.setActive(false);
        Salary newSalary = new Salary(
                user,
                branch,
                totalSalary - salary.getPayedSum(),
                true,
                now,
                now
        );
        try {
            salaryCountRepository.deleteAllByAgreement_UserIdAndBranchId(user.getId(), branch.getId());
            workTimeRepository.deleteAllByUserIdAndBranchIdAndActiveFalse(user.getId(), branch.getId());
            salaryRepository.save(salary);
            salaryRepository.save(newSalary);
            return new ApiResponse("SUCCESS", true);
        } catch (Exception e) {
            return new ApiResponse("ERROR", false);
        }
    }

    public ApiResponse payAvans(UUID salaryId, SalaryDto salaryDto) {
        double payedSum = salaryDto.getSalary();
        Optional<Salary> optionalSalary = salaryRepository.findByIdAndActiveTrue(salaryId);
        if (optionalSalary.isEmpty()) return new ApiResponse("NOT FOUND SALARY", false);
        Salary salary = optionalSalary.get();
        salary.setPayedSum(salary.getPayedSum() + payedSum);
        salary.setDescription(salaryDto.getDescription());
        salary.setEndDate(new Date());
        salaryRepository.save(salary);
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse getAll(UUID branchId) {
        if (!branchRepository.existsById(branchId))return new ApiResponse("NOT FOUND BRANCH");
        List<Salary> salaryList = salaryRepository.findAllByBranchIdAndActiveTrue(branchId);
        if (salaryList.isEmpty())return new ApiResponse("NOT FOUND SALARY");
        return new ApiResponse( true, salaryMapper.toDtoList(salaryList));
    }

    public ApiResponse getOne(UUID salaryId) {
        Optional<Salary> optionalSalary = salaryRepository.findByIdAndActiveTrue(salaryId);
        return optionalSalary.map(salary -> new ApiResponse(true, salaryMapper.toDto(salary))).orElseGet(() -> new ApiResponse("SALARY NOT FOUND", false));
    }

    public ApiResponse getAllByUser(UUID userId, UUID branchId) {
        if (!userRepository.existsById(userId))return new ApiResponse("NOT FOUND USER");
        if (!branchRepository.existsById(branchId))return new ApiResponse("NOT FOUND BRANCH");
        List<Salary> salaryList = salaryRepository.findAllByUserIdAndBranchId(userId, branchId);
        if (salaryList.isEmpty())return new ApiResponse("NOT FOUND SALARY");
        return new ApiResponse(true, salaryMapper.toDtoList(salaryList));
    }
}
