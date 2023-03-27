package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Agreement;
import uz.pdp.springsecurity.entity.Branch;
import uz.pdp.springsecurity.entity.SalaryCount;
import uz.pdp.springsecurity.mapper.SalaryCountMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.SalaryCountDto;
import uz.pdp.springsecurity.repository.AgreementRepository;
import uz.pdp.springsecurity.repository.BranchRepository;
import uz.pdp.springsecurity.repository.SalaryCountRepository;
import uz.pdp.springsecurity.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalaryCountService {
    private final SalaryCountRepository salaryCountRepository;
    private final UserRepository userRepository;
    private final AgreementRepository agreementRepository;
    private final SalaryCountMapper salaryCountMapper;
    private final BranchRepository branchRepository;
    private final SalaryService salaryService;

    public ApiResponse add(SalaryCountDto salaryCountDto) {
        return addEdit(new SalaryCount(), salaryCountDto);
    }

    public ApiResponse edit(UUID salaryCountId, SalaryCountDto salaryCountDto) {
        Optional<SalaryCount> optionalSalaryCount = salaryCountRepository.findById(salaryCountId);
        return optionalSalaryCount.map(salaryCount -> addEdit(salaryCount, salaryCountDto)).orElseGet(() -> new ApiResponse("SALARY COUNT NOT FOUND", false));
    }

    private ApiResponse addEdit(SalaryCount salaryCount, SalaryCountDto salaryCountDto) {
        Optional<Branch> optionalBranch = branchRepository.findById(salaryCountDto.getBranchId());
        if (optionalBranch.isEmpty())return new ApiResponse("NOT FOUND BRANCH");
        Optional<Agreement> optionalAgreement = agreementRepository.findById(salaryCountDto.getAgreementId());
        if (optionalAgreement.isEmpty())return new ApiResponse("AGREEMENT NOT FOUND", false);
        Agreement agreement = optionalAgreement.get();
        Branch branch = optionalBranch.get();
        double salarySum = salaryCountDto.getSalary() - salaryCount.getSalary();
        salaryCount.setCount(salaryCountDto.getCount());
        salaryCount.setSalary(salaryCountDto.getSalary());
        salaryCount.setDate(salaryCountDto.getDate());
        salaryCount.setAgreement(agreement);
        salaryCount.setBranch(branch);
        salaryCountRepository.save(salaryCount);
        salaryService.add(agreement.getUser(), branch, salarySum);
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse getByUserLastMonth(UUID userId, UUID branchId) {
        if (!userRepository.existsById(userId)) return new ApiResponse("USER NOT FOUND", false);
        if (!branchRepository.existsById(branchId)) return new ApiResponse("USER NOT BRANCH", false);
        List<SalaryCount> salaryCountList = salaryCountRepository.findAllByAgreement_UserIdAndBranch_IdOrderByDate(userId, branchId);
        if (salaryCountList.isEmpty())return new ApiResponse("SALARY COUNT NOT FOUND", false);
        return new ApiResponse(true, salaryCountMapper.toGetDtoList(salaryCountList));
    }

    public ApiResponse getOne(UUID salaryCountId) {
        Optional<SalaryCount> optionalSalaryCount = salaryCountRepository.findById(salaryCountId);
        return optionalSalaryCount.map(salaryCount -> new ApiResponse(true, salaryCountMapper.toGetDto(salaryCount))).orElseGet(() -> new ApiResponse("SALARY COUNT NOT FOUND", false));
    }
}
