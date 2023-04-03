package uz.pdp.springsecurity.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Agreement;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.enums.SalaryStatus;
import uz.pdp.springsecurity.payload.AgreementDto;
import uz.pdp.springsecurity.payload.AgreementGetDto;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.repository.AgreementRepository;

import java.util.*;

import static uz.pdp.springsecurity.enums.SalaryStatus.*;

@Service
@RequiredArgsConstructor
public class AgreementService {
    private final AgreementRepository agreementRepository;
    public void add(User user) {
        List<Agreement> agreementList =
                Arrays.asList(
                        new Agreement(user, HOUR, 0d, false),
                        new Agreement(user, DAY, 0d, false),
                        new Agreement(user, MONTH, 0d, false),
                        new Agreement(user, KPI, 0d, true)
                );
        agreementRepository.saveAll(agreementList);
    }

    public ApiResponse edit(UUID userId, AgreementGetDto agreementGetDto) {
        Integer countAllByUserId = agreementRepository.countAllByUserId(userId);
        if (countAllByUserId != SalaryStatus.values().length) return new ApiResponse("ERROR", false);
        if (countAllByUserId != agreementGetDto.getAgreementDtoList().size()) return new ApiResponse("LIST SIZE ERROR", false);
        List<Agreement> agreementList = new ArrayList<>();
        for (AgreementDto agreementDto : agreementGetDto.getAgreementDtoList()) {
            Optional<Agreement> optionalAgreement = agreementRepository.findByUserIdAndSalaryStatus(userId, SalaryStatus.valueOf(agreementDto.getSalaryStatus()));
            Agreement agreement = editHelper(optionalAgreement, agreementDto);
            if (agreement == null) return new ApiResponse("AGREEMENT NOT FOUND", false);
            agreementList.add(agreement);
        }
        agreementRepository.saveAll(agreementList);
        return new ApiResponse("SUCCESS", true);
    }

    private Agreement editHelper(Optional<Agreement> optionalAgreement, AgreementDto agreementDto) {
        if (optionalAgreement.isEmpty()) return null;
        Agreement agreement = optionalAgreement.get();
        agreement.setActive(agreementDto.isActive());
        agreement.setPrice(agreementDto.getPrice());
        return agreement;
    }


    public ApiResponse getOne(UUID userId) {
        List<Agreement> agreementList = agreementRepository.findAllByUserId(userId);
        if (agreementList.size() != SalaryStatus.values().length) return new ApiResponse("ERROR", false);
        List<AgreementDto> agreementDtoList = new ArrayList<>();
        for (Agreement agreement : agreementList) {
            agreementDtoList.add(
                    new AgreementDto(
                            agreement.getId(),
                            agreement.getSalaryStatus().name(),
                            agreement.getPrice(),
                            agreement.isActive())
            );
        }
        AgreementGetDto agreementGetDto = new AgreementGetDto(
                agreementDtoList
        );
        return new ApiResponse( true, agreementGetDto);
    }
}
