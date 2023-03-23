package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.LidField;
import uz.pdp.springsecurity.mapper.LidFieldMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LidFieldDto;
import uz.pdp.springsecurity.repository.LidFieldRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LidFieldService {

    private final LidFieldRepository repository;

    private final LidFieldMapper mapper;


    public ApiResponse getAll(UUID businessId) {
        List<LidField> allByBusinessId =
                repository.findAllByBusiness_Id(businessId);

        List<LidField> allByBusinessIsNull =
                repository.findAllByBusinessIsNull();

        allByBusinessId.addAll(allByBusinessIsNull);
        List<LidFieldDto> lidFieldDtoList = mapper.toDto(allByBusinessId);

        if (lidFieldDtoList.isEmpty()) {
            return new ApiResponse("not found", false);
        }

        return new ApiResponse("found", true, lidFieldDtoList);
    }

    //todo mapper ishlamasa enum qolda berilsin
    public ApiResponse create(LidFieldDto lidFieldDto) {
        LidField lidField = mapper.toEntity(lidFieldDto);
        repository.save(lidField);
        return new ApiResponse("successfully saved", true);
    }

    
    public ApiResponse edit(UUID id, LidFieldDto lidFieldDto) {
        Optional<LidField> optional = repository.findById(id);

        if (optional.isEmpty()) {
            return new ApiResponse("not found", false);
        }

        LidField lidField = optional.get();
        mapper.update(lidFieldDto, lidField);
        repository.save(lidField);

        return new ApiResponse("successfully edit", true);
    }


    //todo yana korib chqilsin
    public ApiResponse delete(UUID id) {
        Optional<LidField> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return new ApiResponse("not found", false);
        }

        LidField lidField = optional.get();

        repository.delete(lidField);

        return new ApiResponse("successfully deleted", true);
    }
}
