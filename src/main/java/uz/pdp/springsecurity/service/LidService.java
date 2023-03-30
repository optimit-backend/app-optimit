package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.NotificationType;
import uz.pdp.springsecurity.mapper.LidMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LidDto;
import uz.pdp.springsecurity.payload.LidGetDto;
import uz.pdp.springsecurity.repository.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LidService {
    private final LidRepository repository;
    private final LidStatusRepository lidStatusRepository;
    private final LidMapper mapper;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final LidFieldRepository lidFieldRepository;
    private final BusinessRepository businessRepository;

    public ApiResponse getAll(UUID businessId) {
        List<Lid> all = repository.findAllByBusinessId(businessId);
        List<LidGetDto> dtoList = getDtoList(all);
        return new ApiResponse("found", true, dtoList);
    }

    public ApiResponse getById(UUID id) {
        Lid lid = repository.findById(id).orElse(null);
        if (lid == null) {
            return new ApiResponse("not found", false);
        }
        LidGetDto lidGetDto = getDto(lid);
        return new ApiResponse("found", true, lidGetDto);
    }


    public ApiResponse create(LidDto lidDto) {
        User admin = userRepository.findByBusinessIdAndRoleName(lidDto.getBusinessId(), "ADMIN").orElse(null);
        Map<UUID, String> values = lidDto.getValues();

        Map<LidField, String> value = new HashMap<>();
        for (Map.Entry<UUID, String> uuidStringEntry : values.entrySet()) {
            lidFieldRepository.findById(uuidStringEntry.getKey()).ifPresent(lidField -> value.put(lidField, uuidStringEntry.getValue()));
        }
        Optional<LidStatus> optional = lidStatusRepository.findByName("New");
        optional.ifPresent(lidStatus -> lidDto.setLidStatusId(lidStatus.getId()));

        Lid lid = mapper.toEntity(lidDto);
        lid.setValues(value);
        repository.save(lid);
        if (admin != null) {
            Notification notification = new Notification();
            notification.setRead(false);
            notification.setName("Yangi lid qo'shildi!");
            notification.setMessage("Yangi lid qo'shildi kirib ko'rishingiz statuslarga ob o'tishingiz mumkin!");
            notification.setUserTo(admin);
            notification.setType(NotificationType.NEW_LID);
            notification.setObjectId(lid.getId());
            notificationRepository.save(notification);
        }
        return new ApiResponse("successfully saved", true);
    }

    public ApiResponse editStatus(UUID id, UUID statusId) {
        Lid lid = repository.findById(id).orElse(null);
        if (lid == null) {
            return new ApiResponse("not found lid", false);
        }
        LidStatus lidStatus = lidStatusRepository.findById(statusId).orElse(null);
        if (lidStatus == null) {
            return new ApiResponse("not found lid status", false);
        }

        lid.setLidStatus(lidStatus);
        repository.save(lid);
        return new ApiResponse("successfully edited", true);
    }

    public ApiResponse delete(UUID id) {
        Lid lid = repository.findById(id).orElse(null);
        if (lid == null) {
            return new ApiResponse("not found", false);
        }
        repository.delete(lid);
        return new ApiResponse("successfully saved", true);
    }

    private LidGetDto getDto(Lid lid) {
        if (lid == null) {
            return null;
        }

        LidGetDto lidGetDto = new LidGetDto();
        LidStatus lidStatus = lidStatusRepository.findById(lid.getLidStatus().getId()).orElse(null);
        Business business = businessRepository.findById(lid.getBusiness().getId()).orElse(null);


        lidGetDto.setId(lid.getId());
        lidGetDto.setLidStatusId(lidStatus != null ? lidStatus.getId() : null);
        lidGetDto.setBusinessId(business != null ? business.getId() : null);

        Map<LidField, String> lidValues = lid.getValues();
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<LidField, String> entry : lidValues.entrySet()) {
            values.put(entry.getKey().getName(), entry.getValue());
        }
        lidGetDto.setValues(values);

        return lidGetDto;
    }

    public ApiResponse getByBusinessIdPageable(UUID id) {
        List<LidStatus> all = lidStatusRepository.findAllByOrderBySortAsc();
        List<LidStatus> allStatus = lidStatusRepository.findAllByBusiness_IdOrderBySortAsc(id);
        allStatus.addAll(all);
        List<Map<String, Object>> responses = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 10);

        for (LidStatus status : allStatus) {
            Page<Lid> allLid = repository.findAllByBusiness_IdAndLidStatusId(id, status.getId(), pageable);
            List<LidGetDto> lidGetDtoList = getDtoList(allLid.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("statusId", status.getId());
            response.put("getLessProduct", lidGetDtoList);
            response.put("currentPage", allLid.getNumber());
            response.put("totalItems", allLid.getTotalElements());
            response.put("totalPages", allLid.getTotalPages());
            responses.add(response);
        }

        return new ApiResponse("found", true, responses);
    }

    private List<LidGetDto> getDtoList(List<Lid> lidList) {
        if (lidList == null) {
            return null;
        }

        List<LidGetDto> list = new ArrayList<LidGetDto>(lidList.size());
        for (Lid lid : lidList) {
            list.add(getDto(lid));
        }

        return list;
    }
}
