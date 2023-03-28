package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.SelectForLid;
import uz.pdp.springsecurity.mapper.SelectForLidMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.SelectForLidDto;
import uz.pdp.springsecurity.repository.SelectForLidRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SelectForLidService {
    private final SelectForLidRepository repository;

    private final SelectForLidMapper mapper;

    public ApiResponse getAll(UUID businessId) {
        List<SelectForLid> all = repository.findAllByLid_BusinessId(businessId);
        if (all.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        List<SelectForLidDto> allDto = mapper.toDto(all);
        return new ApiResponse("found", true, allDto);
    }

    public ApiResponse getById(UUID id) {
        SelectForLid selectForLid = repository.findById(id).orElse(null);
        if (selectForLid == null) {
            return new ApiResponse("not found", false);
        }
        return new ApiResponse("found", true, mapper.toDto(selectForLid));
    }

    public ApiResponse create(SelectForLidDto dto) {
        repository.save(mapper.toEntity(dto));
        return new ApiResponse("successfully saved", true);
    }

    public ApiResponse edit(UUID id, SelectForLidDto dto) {
        SelectForLid selectForLid = repository.findById(id).orElse(null);
        if (selectForLid == null) {
            return new ApiResponse("not found", false);
        }
        mapper.update(dto, selectForLid);
        return new ApiResponse("successfully edited", true);
    }

    public ApiResponse delete(UUID id) {
        SelectForLid selectForLid = repository.findById(id).orElse(null);
        if (selectForLid == null) {
            return new ApiResponse("not found", false);
        }
        repository.delete(selectForLid);
        return new ApiResponse("successfully deleted", true);
    }
}
