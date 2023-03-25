package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.LidStatus;
import uz.pdp.springsecurity.mapper.LidStatusMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LidStatusDto;
import uz.pdp.springsecurity.repository.LidStatusRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LidStatusService {
    private final LidStatusRepository repository;
    private final LidStatusMapper mapper;

    public ApiResponse getAll(UUID businessId) {
        List<LidStatus> allByBusinessId =
                repository.findAllByBusiness_Id(businessId);

        if (allByBusinessId.isEmpty()) {
            return new ApiResponse("not found", false);
        }

        return new ApiResponse("found", true, mapper.toDto(allByBusinessId));
    }

    public ApiResponse getById(UUID id) {
        Optional<LidStatus> optional = repository.findById(id);
        return optional.map(lidStatus ->
                new ApiResponse("found", true, mapper.toDto(lidStatus))).orElseGet(() -> new ApiResponse("not found", false));
    }

    public ApiResponse create(LidStatusDto lidStatusDto) {
        repository.save(mapper.toEntity(lidStatusDto));
        return new ApiResponse("successfully saved", true);
    }

    public ApiResponse edit(UUID id, LidStatusDto lidStatusDto) {
        Optional<LidStatus> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return new ApiResponse("not found", false);
        }

        LidStatus lidStatus = optional.get();
        mapper.update(lidStatusDto, lidStatus);
        repository.save(lidStatus);
        return new ApiResponse("successfully edited", true);
    }

    public ApiResponse delete(UUID id) {
        Optional<LidStatus> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        LidStatus lidStatus = optional.get();
        repository.delete(lidStatus);
        return new ApiResponse("successfully deleted", true);
    }
}
