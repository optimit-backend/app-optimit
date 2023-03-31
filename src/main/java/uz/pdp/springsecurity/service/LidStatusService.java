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
                repository.findAllByBusiness_IdOrderBySortAsc(businessId);
        List<LidStatus> all = repository.findAllByBusinessIsNullOrderBySortAsc();
        all.addAll(allByBusinessId);

        if (allByBusinessId.isEmpty()) {
            return new ApiResponse("not found", false);
        }

        return new ApiResponse("found", true, mapper.toDto(all));
    }

    public ApiResponse getById(UUID id) {
        Optional<LidStatus> optional = repository.findById(id);
        return optional.map(lidStatus ->
                new ApiResponse("found", true, mapper.toDto(lidStatus))).orElseGet(() -> new ApiResponse("not found", false));
    }

    public ApiResponse create(LidStatusDto lidStatusDto) {
        List<LidStatus> allByOrderBySortDesc = repository.findAllByBusiness_IdOrderBySortAsc(lidStatusDto.getBusinessId());
        LidStatus newLidStatus = mapper.toEntity(lidStatusDto);
        if (allByOrderBySortDesc.size() != 0) {
            LidStatus lidStatus = allByOrderBySortDesc.get(allByOrderBySortDesc.size() - 1);
            Integer sort = lidStatus.getSort();
            newLidStatus.setSort(++sort);
        } else {
            newLidStatus.setSort(1);
        }
        newLidStatus.setBig(true);
        repository.save(newLidStatus);
        return new ApiResponse("successfully saved", true);
    }

    public ApiResponse edit(UUID id, LidStatusDto lidStatusDto) {
        LidStatus lidStatus = repository.findById(id).orElse(null);
        if (lidStatus == null) {
            return new ApiResponse("not found", false);
        }
        List<LidStatus> all = repository.
                findAllByBusiness_IdOrderBySortAsc(lidStatusDto.getBusinessId());

        Integer currentSort = lidStatus.getSort();
        Integer newSort = lidStatusDto.getSort();

        if (currentSort < newSort) {
            for (LidStatus status : all) {
                if (status.getSort() > currentSort && status.getSort() <= newSort) {
                    status.setSort(status.getSort() - 1);
                    repository.save(status);
                }
            }
        } else {
            for (LidStatus status : all) {
                if (status.getSort() <= currentSort && status.getSort() > newSort) {
                    status.setSort(status.getSort() + 1);
                    repository.save(status);
                }
            }
        }

        mapper.update(lidStatusDto, lidStatus);
        lidStatus.setSort(newSort);
        lidStatus.setBig(lidStatus.isBig());
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
