package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Lid;
import uz.pdp.springsecurity.entity.LidStatus;
import uz.pdp.springsecurity.entity.Notification;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.enums.NotificationType;
import uz.pdp.springsecurity.mapper.LidMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LidDto;
import uz.pdp.springsecurity.repository.LidRepository;
import uz.pdp.springsecurity.repository.LidStatusRepository;
import uz.pdp.springsecurity.repository.NotificationRepository;
import uz.pdp.springsecurity.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LidService {
    private final LidRepository repository;
    private final LidStatusRepository lidStatusRepository;
    private final LidMapper mapper;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public ApiResponse getAll(UUID businessId) {
        List<LidStatus> allLidStatus =
                lidStatusRepository.findAllByBusiness_IdOrderBySortAsc(businessId);


        return null;
    }

    public ApiResponse getById(UUID id) {
        Lid lid = repository.findById(id).orElse(null);
        if (lid == null) {
            return new ApiResponse("not found", false);
        }
        LidDto lidDto = mapper.toDto(lid);
        return new ApiResponse("found", true, lidDto);
    }

    public ApiResponse create(LidDto lidDto) {
        User admin = userRepository.findByBusinessIdAndRoleName(lidDto.getLidStatus().getBusiness().getId(), "ADMIN").orElse(null);
        Lid lid = repository.save(mapper.toEntity(lidDto));
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
        return new ApiResponse("successfully edited", false);
    }

    public ApiResponse delete(UUID id) {
        Lid lid = repository.findById(id).orElse(null);
        if (lid == null) {
            return new ApiResponse("not found", false);
        }
        repository.save(lid);
        return new ApiResponse("successfully saved", true);
    }
}
