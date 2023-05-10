package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.StatusTariff;
import uz.pdp.springsecurity.payload.NotificationDto;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.ShablonRepository;
import uz.pdp.springsecurity.repository.SubscriptionRepository;
import uz.pdp.springsecurity.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScheduleTaskService {

    private final SubscriptionRepository subscriptionRepository;

    private final static LocalDateTime TODAY = LocalDate.now().atStartOfDay();
    private final static LocalDateTime END_TODAY = LocalDateTime.of(TODAY.getYear(), TODAY.getMonth(), TODAY.getDayOfMonth(), 23, 59, 59);
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ShablonRepository shablonRepository;
    private final NotificationService notificationService;


    @Scheduled(cron = "0 0/46 10 * * *")
    public void execute() {

        List<Subscription> subscriptions = subscriptionRepository
                .findAllByEndDayBetweenAndDeleteIsFalse(Timestamp.valueOf(TODAY), Timestamp.valueOf(END_TODAY));

        for (Subscription subscription : subscriptions) {
            subscription.setDelete(true);
            subscription.setActive(false);
            subscriptionRepository.save(subscription);

            UUID businessId = subscription.getBusiness().getId();
            Business business = subscription.getBusiness();
            Tariff tariff = subscription.getTariff();

            Optional<Subscription> optionalSubscription = subscriptionRepository
                    .findByStartDayBetweenAndBusinessIdAndDeleteIsFalse(Timestamp.valueOf(TODAY), Timestamp.valueOf(END_TODAY)
                            , businessId);

            Subscription newSubscription;

            if (optionalSubscription.isPresent()) {
                newSubscription = optionalSubscription.get();
                newSubscription.setActive(true);
            } else {
                newSubscription = new Subscription();
                newSubscription.setStatusTariff(StatusTariff.WAITING);
                newSubscription.setBusiness(business);
                newSubscription.setTariff(tariff);
                newSubscription.setActive(false);
                newSubscription.setDelete(false);
                newSubscription.setActiveNewTariff(false);
            }
            subscriptionRepository.save(newSubscription);
        }
    }


    @Scheduled(cron = "0 0/22 16 * * *")
    public void Notification() {
        List<Business> all = businessRepository.findAll();
        for (Business business : all) {
            Date today = Date.from(TODAY.atZone(ZoneId.systemDefault()).toInstant());
            Date endToday = Date.from(END_TODAY.atZone(ZoneId.systemDefault()).toInstant());
            List<User> allUser = userRepository.findAllByBusiness_IdAndBirthdayBetween(business.getId(), today, endToday);
            for (User user : allUser) {
                Optional<Shablon> birthday = shablonRepository.findByOriginalNameAndBusiness_Id("bithday", business.getId());
                if (birthday.isPresent()) {
                    Shablon shablon = birthday.get();
                    NotificationDto notificationDto = new NotificationDto();
                    notificationDto.setNotificationKay("select");
                    notificationDto.setName(shablon.getName());
                    notificationDto.setUserToId(List.of(user.getId()));
                    notificationDto.setShablonId(shablon.getId());
                    notificationService.create(notificationDto);
                }
            }
        }
    }
}

