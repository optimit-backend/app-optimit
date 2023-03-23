package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.Subscription;
import uz.pdp.springsecurity.entity.Tariff;
import uz.pdp.springsecurity.enums.StatusTariff;
import uz.pdp.springsecurity.repository.SubscriptionRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScheduleTaskService {

    private final SubscriptionRepository subscriptionRepository;

    private final static LocalDateTime TODAY = LocalDate.now().atStartOfDay();
    private final static LocalDateTime END_TODAY = LocalDateTime.of(TODAY.getYear(), TODAY.getMonth(), TODAY.getDayOfMonth(), 23, 59, 59);


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

            if(optionalSubscription.isPresent()) {
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
}

