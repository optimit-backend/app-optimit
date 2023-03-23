package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Notification;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.mapper.NotificationMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.NotificationGetByIdDto;
import uz.pdp.springsecurity.repository.NotificationRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    private final NotificationMapper mapper;

    public ApiResponse getAll(User user) {
        UUID userId = user.getId();

        List<Notification> allByReadIsFalse = repository.findAllByReadIsFalseAndUserToId(userId);
        List<Notification> allByReadIsTrue = repository.findAllByReadIsTrueAndUserToId(userId);

        allByReadIsFalse.sort(Comparator.comparing(Notification::getCreatedAt));
        allByReadIsTrue.sort(Comparator.comparing(Notification::getCreatedAt));

        List<Notification> notificationList = new ArrayList<>(allByReadIsFalse);
        notificationList.addAll(allByReadIsTrue);

        if (notificationList.isEmpty()) {
            return new ApiResponse("notification empty", false);
        }

        return new ApiResponse("all notification", true, mapper.toDtoGetAll(notificationList));
    }

    public ApiResponse getById(UUID id) {
        Optional<Notification> byId = repository.findById(id);
        if (byId.isEmpty()) {
            return new ApiResponse("not found", false);
        }

        Notification notification = byId.get();
        notification.setRead(true);
        repository.save(notification);
        NotificationGetByIdDto notificationGetByIdDto = mapper.toDtoGetById(notification);
        notificationGetByIdDto.setType(notification.getType().name());

        return new ApiResponse("found", true, notificationGetByIdDto);
    }

    public ApiResponse delete(User user) {
        List<Notification> allByReadIsTrue = repository.findAllByReadIsTrueAndUserToId(user.getId());
        if (!allByReadIsTrue.isEmpty()) {
            repository.deleteAll(allByReadIsTrue);
        }

        return new ApiResponse("deleted", true);
    }
}
