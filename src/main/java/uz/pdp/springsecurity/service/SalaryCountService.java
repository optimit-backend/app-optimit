package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Agreement;
import uz.pdp.springsecurity.entity.SalaryCount;
import uz.pdp.springsecurity.mapper.SalaryCountMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.SalaryCountDto;
import uz.pdp.springsecurity.repository.AgreementRepository;
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

    public ApiResponse add(SalaryCountDto salaryCountDto) {
        Optional<Agreement> optionalAgreement = agreementRepository.findById(salaryCountDto.getAgreementId());
        if (optionalAgreement.isEmpty())return new ApiResponse("AGREEMENT NOT FOUND", false);
        SalaryCount salaryCount = salaryCountRepository.save(
                new SalaryCount(
                        salaryCountDto.getCount(),
                        salaryCountDto.getSalary(),
                        optionalAgreement.get(),
                        salaryCountDto.getDate()
                )
        );
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse edit(UUID salaryCountId, SalaryCountDto salaryCountDto) {
        Optional<SalaryCount> optionalSalaryCount = salaryCountRepository.findById(salaryCountId);
        if (optionalSalaryCount.isEmpty())return new ApiResponse("SALARY COUNT NOT FOUND", false);
        Optional<Agreement> optionalAgreement = agreementRepository.findById(salaryCountDto.getAgreementId());
        if (optionalAgreement.isEmpty())return new ApiResponse("AGREEMENT NOT FOUND", false);
        SalaryCount salaryCount = optionalSalaryCount.get();
        salaryCount.setCount(salaryCountDto.getCount());
        salaryCount.setSalary(salaryCountDto.getSalary());
        salaryCount.setDate(salaryCountDto.getDate());
        salaryCount.setAgreement(optionalAgreement.get());
        salaryCountRepository.save(salaryCount);
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse getByUserLastMonth(UUID userId) {
        if (!userRepository.existsById(userId)) return new ApiResponse("USER NOT FOUND", false);
        List<SalaryCount> salaryCountList = salaryCountRepository.findAllByAgreement_UserIdOrderByDate(userId);
        if (salaryCountList.isEmpty())return new ApiResponse("SALARY COUNT NOT FOUND", false);
        return new ApiResponse(true, salaryCountMapper.toGetDtoList(salaryCountList));
    }

    public ApiResponse getOne(UUID salaryCountId) {
        Optional<SalaryCount> optionalSalaryCount = salaryCountRepository.findById(salaryCountId);
        return optionalSalaryCount.map(salaryCount -> new ApiResponse(true, salaryCountMapper.toDto(salaryCount))).orElseGet(() -> new ApiResponse("SALARY COUNT NOT FOUND", false));
    }
}
