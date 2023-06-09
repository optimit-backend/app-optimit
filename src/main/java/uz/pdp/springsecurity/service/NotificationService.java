package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.NotificationType;
import uz.pdp.springsecurity.mapper.NotificationMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.NotificationDto;
import uz.pdp.springsecurity.payload.NotificationGetByIdDto;
import uz.pdp.springsecurity.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    private final NotificationMapper mapper;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final ShablonRepository shablonRepository;
    private final ProductRepository productRepository;
    private final ProductTypePriceRepository productTypePriceRepository;

    public ApiResponse getAll(User user) {
        UUID userId = user.getId();

        List<Notification> allByReadIsFalse = repository.findAllByReadIsFalseAndUserToId(userId);
        List<Notification> allByReadIsTrue = repository.findAllByReadIsTrueAndUserToId(userId);

        allByReadIsFalse.sort(Comparator.comparing(Notification::getCreatedAt).reversed());
        allByReadIsTrue.sort(Comparator.comparing(Notification::getCreatedAt).reversed());

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
        if (notification.getAttachment() != null) {
            notificationGetByIdDto.setAttachmentId(notification.getAttachment().getId());
        }

        return new ApiResponse("found", true, notificationGetByIdDto);
    }

    public ApiResponse delete(User user) {
        List<Notification> allByReadIsTrue = repository.findAllByReadIsTrueAndUserToId(user.getId());
        if (!allByReadIsTrue.isEmpty()) {
            repository.deleteAll(allByReadIsTrue);
        }

        return new ApiResponse("deleted", true);
    }

    public ApiResponse create(NotificationDto notificationDto) {
        List<UUID> userToIdList = new ArrayList<>();

        assert notificationDto.getNotificationKay() != null;
        if (notificationDto.getNotificationKay().equals("ALL")) {
            List<User> allByBusinessId = new ArrayList<>();
            UUID businessOrBranchId = notificationDto.getBusinessOrBranchId();
            allByBusinessId.addAll(userRepository.findAllByBusiness_Id(businessOrBranchId));
            allByBusinessId.addAll(userRepository.findAllByBranches_Id(businessOrBranchId));
            for (User user : allByBusinessId) {
                userToIdList.add(user.getId());
            }
        } else {
            userToIdList = notificationDto.getUserToId();
        }

        if (userToIdList.isEmpty()) {
            return new ApiResponse("Userlarni belgilang", false);
        }

        for (UUID id : userToIdList) {
            if (!id.equals(notificationDto.getUserFromId())) {
                Notification notification = new Notification();
                notification.setName("Yangi Xabar keldi!");
                if (notificationDto.getShablonId() != null) {
                    Optional<Shablon> optionalShablon = shablonRepository.findById(notificationDto.getShablonId());
                    if (optionalShablon.isPresent()) {
                        Shablon shablon = optionalShablon.get();
                        notification.setMessage(shablon.getMessage());
                    }
                } else {
                    notification.setMessage(notificationDto.getMessage());
                }
                notification.setType(NotificationType.NOTIFICATION);
                if (notificationDto.getUserFromId() != null) {
                    userRepository.findById(notificationDto.getUserFromId()).ifPresent(notification::setUserFrom);
                }
                userRepository.findById(id).ifPresent(notification::setUserTo);
                if (notificationDto.getAttachmentId() != null) {
                    attachmentRepository.findById(notificationDto.getAttachmentId()).ifPresent(notification::setAttachment);
                }
                repository.save(notification);
            }
        }

        return new ApiResponse("Xabarlar jo'natildi!", true);
    }

    public void lessProduct(UUID productId, boolean isProduct, double amount) {
        if (isProduct) {
            Optional<Product> optionalProduct = productRepository.findById(productId);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                String name = product.getName();
                Notification notification = new Notification();
                notification.setName("Oz qolgan maxsulotlar");
                notification.setObjectId(product.getId());
                notification.setMessage(name + " maxsulotdan " + amount + " ta qoldi!");
                Optional<User> optionalUser = userRepository.
                        findByBusinessIdAndRoleName(product.getBusiness().getId(), "Admin");
                optionalUser.ifPresent(notification::setUserTo);
                notification.setObjectId(product.getId());
                notification.setType(NotificationType.LESS_PRODUCT);
                repository.save(notification);
            }
        } else {
            Optional<ProductTypePrice> optionalProductTypePrice = productTypePriceRepository.findById(productId);
            if (optionalProductTypePrice.isPresent()) {
                ProductTypePrice productTypePrice = optionalProductTypePrice.get();
                String name = productTypePrice.getName();
                Notification notification = new Notification();
                notification.setName("Oz qolgan maxsulotlar");
                notification.setObjectId(productTypePrice.getId());
                notification.setMessage(name + " maxsulotdan " + amount + " ta qoldi!");
                Optional<User> optionalUser = userRepository.
                        findByBusinessIdAndRoleName(productTypePrice.getProduct().getBusiness().getId(), "Admin");
                optionalUser.ifPresent(notification::setUserTo);
                notification.setObjectId(productTypePrice.getProduct().getId());
                notification.setType(NotificationType.LESS_PRODUCT);
                repository.save(notification);
            }
        }
    }
}
