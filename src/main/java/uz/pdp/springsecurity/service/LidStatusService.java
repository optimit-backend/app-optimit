package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Lid;
import uz.pdp.springsecurity.entity.LidStatus;
import uz.pdp.springsecurity.mapper.LidStatusMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LidStatusDto;
import uz.pdp.springsecurity.payload.LidStatusPostDto;
import uz.pdp.springsecurity.repository.LidRepository;
import uz.pdp.springsecurity.repository.LidStatusRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LidStatusService {
    private final LidStatusRepository repository;
    private final LidStatusMapper mapper;
    private final LidRepository lidRepository;

    public ApiResponse getAll(UUID businessId) {

        List<LidStatus> allByBusinessId =
                repository.findAllByBusiness_IdOrderBySortAsc(businessId);

        List<LidStatusDto> list = new ArrayList<>();
        for (LidStatus lidStatus : allByBusinessId) {
            LidStatusDto statusDto = mapper.toDto(lidStatus);
            int count = lidRepository.countByLidStatusId(lidStatus.getId());
            statusDto.setNumberOfLids(count);
            list.add(statusDto);
        }
        list.sort(Comparator.comparing(LidStatusDto::getSort));

        if (allByBusinessId.isEmpty()) {
            return new ApiResponse("not found", false);
        }

        return new ApiResponse("found", true, list);
    }

    public ApiResponse getById(UUID id) {
        Optional<LidStatus> optional = repository.findById(id);
        return optional.map(lidStatus ->
                new ApiResponse("found", true, mapper.toDto(lidStatus))).orElseGet(() -> new ApiResponse("not found", false));
    }

    public ApiResponse create(LidStatusPostDto lidStatusPostDto) {
        List<LidStatus> allByOrderBySortDesc = repository.findAllByBusiness_IdOrderBySortAsc(lidStatusPostDto.getBusinessId());
        LidStatus newLidStatus = mapper.toEntity(lidStatusPostDto);
        if (allByOrderBySortDesc.size() != 0) {
            LidStatus lidStatus = allByOrderBySortDesc.get(allByOrderBySortDesc.size() - 1);
            Integer sort = lidStatus.getSort();
            newLidStatus.setSort(++sort);
        } else {
            newLidStatus.setSort(1);
        }
        newLidStatus.setIncrease(true);
        repository.save(newLidStatus);
        return new ApiResponse("successfully saved", true);
    }

    public ApiResponse edit(UUID id, LidStatusPostDto lidStatusPostDto) {
        LidStatus lidStatus = repository.findById(id).orElse(null);
        if (lidStatus == null) {
            return new ApiResponse("not found", false);
        }
        List<LidStatus> all = repository.
                findAllByBusiness_IdOrderBySortAsc(lidStatusPostDto.getBusinessId());

        Integer currentSort = lidStatus.getSort();
        Integer newSort = lidStatusPostDto.getSort();

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

        mapper.update(lidStatusPostDto, lidStatus);
        lidStatus.setSort(newSort);
        lidStatus.setIncrease(lidStatus.isIncrease());
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

        List<LidStatus> allByBusinessId =
                repository.findAllByBusiness_IdOrderBySortAsc(optional.get().getBusiness().getId());
        List<LidStatus> all = repository.findAllByBusinessIsNullOrderBySortAsc();
        all.addAll(allByBusinessId);

        all.sort(Comparator.comparing(LidStatus::getSort));

        int index = 1;
        for (LidStatus lidStatus1 : all) {
            if (lidStatus1.getId() != lidStatus.getId()) {
                lidStatus1.setSort(index++);
                repository.save(lidStatus1);
            }
        }
        return new ApiResponse("successfully deleted", true);
    }

    public ApiResponse changeBig(UUID id) {
        Optional<LidStatus> optionalLidStatus = repository.findById(id);
        if (optionalLidStatus.isEmpty()) {
            return new ApiResponse("not found lid status by id", false);
        }
        LidStatus lidStatus = optionalLidStatus.get();
        lidStatus.setIncrease(!lidStatus.isIncrease());
        repository.save(lidStatus);
        return new ApiResponse("successfully saved", true);
    }
}
