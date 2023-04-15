package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.SalaryStatus;
import uz.pdp.springsecurity.mapper.SalaryCountMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.SalaryCountDto;
import uz.pdp.springsecurity.repository.*;

import java.util.Date;
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
    private final TaskRepository taskRepository;

    public ApiResponse add(SalaryCountDto salaryCountDto) {
        Optional<SalaryCount> optionalSalaryCount = salaryCountRepository.findAllByAgreementIdAndBranchId(salaryCountDto.getAgreementId(), salaryCountDto.getBranchId());
        if (optionalSalaryCount.isEmpty())
            return addEdit(new SalaryCount(), salaryCountDto);
        SalaryCount salaryCount = optionalSalaryCount.get();
        salaryCount.setCount(salaryCount.getCount()+ salaryCountDto.getCount());
        salaryCount.setSalary(salaryCount.getSalary()+ salaryCountDto.getSalary());
        salaryCount.setDate(salaryCountDto.getDate());
        salaryCount.setDescription(salaryCountDto.getDescription());
        salaryCountRepository.save(salaryCount);
        salaryService.add(salaryCount.getAgreement().getUser(), salaryCount.getBranch(), salaryCountDto.getSalary());
        return new ApiResponse("SUCCESS", true);
    }

    /*public ApiResponse edit(UUID salaryCountId, SalaryCountDto salaryCountDto) {
        Optional<SalaryCount> optionalSalaryCount = salaryCountRepository.findById(salaryCountId);
        return optionalSalaryCount.map(salaryCount -> addEdit(salaryCount, salaryCountDto)).orElseGet(() -> new ApiResponse("SALARY COUNT NOT FOUND", false));
    }*/

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
        salaryCount.setDescription(salaryCountDto.getDescription());
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

    public void addForTask(Task task) {
        if (task.isGiven()) return;
        if (task.getUsers().size() == 0) return;
        if (task.getTaskPrice() == 0) return;
        task.setGiven(true);
        double salarySum = task.isEach() ? task.getTaskPrice() : (task.getTaskPrice() / task.getUsers().size());
        for (User user : task.getUsers()) {
            Optional<Agreement> optionalAgreement = agreementRepository.findByUserIdAndSalaryStatus(user.getId(), SalaryStatus.KPI);
            if (optionalAgreement.isEmpty()) continue;
            add(new SalaryCountDto(
                    1,
                    salarySum,
                    optionalAgreement.get().getId(),
                    task.getBranch().getId(),
                    new Date(),
                    "vazifa nomi : " + task.getName()
            ));
        }
        taskRepository.save(task);
    }
}
