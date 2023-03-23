package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.NotificationType;
import uz.pdp.springsecurity.enums.StatusTariff;
import uz.pdp.springsecurity.mapper.AddressMapper;
import uz.pdp.springsecurity.mapper.BranchMapper;
import uz.pdp.springsecurity.mapper.BusinessMapper;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;
import uz.pdp.springsecurity.util.Constants;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessService {
    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    TariffRepository tariffRepository;

    @Autowired
    UserService userService;

    private final BranchRepository branchRepository;

    private final AddressRepository addressRepository;

    private final BranchMapper branchMapper;

    private final AddressMapper addressMapper;

    private final SubscriptionRepository subscriptionRepository;

    private final BusinessMapper businessMapper;
    private final PayMethodRepository payMethodRepository;

    private final NotificationRepository notificationRepository;

    private final static LocalDateTime TODAY = LocalDate.now().atStartOfDay();
    private final static LocalDateTime THIS_WEEK = TODAY.minusDays(TODAY.getDayOfWeek().ordinal());
    private final static LocalDateTime THIS_MONTH = LocalDateTime.of(TODAY.getYear(), TODAY.getMonth(), 1, 0, 0, 0);
    private final static LocalDateTime THIS_YEAR = LocalDateTime.of(TODAY.getYear(), 1, 1, 0, 0, 0);

    public ApiResponse add(BusinessDto businessDto) {
        if (businessRepository.existsByNameIgnoreCase(businessDto.getName()))
            return new ApiResponse("A BUSINESS WITH THAT NAME ALREADY EXISTS", false);
        Business business = new Business();
        business.setName(businessDto.getName());
        business.setDescription(businessDto.getDescription());
        UUID tariffId = businessDto.getTariffId();
        Optional<Tariff> optionalTariff = tariffRepository.findById(tariffId);
        business.setActive(businessDto.isActive());
        business.setDelete(false);
        business = businessRepository.save(business);
        Currency currencyUZB = currencyRepository.save(new Currency(
                "SO'M",
                "UZB",
                business,
                true));

        payMethodRepository.save(new PaymentMethod(
                "Naqd",
                business
        ));
        payMethodRepository.save(new PaymentMethod(
                "PlastikKarta",
                business
        ));
        payMethodRepository.save(new PaymentMethod(
                "BankOrqali",
                business
        ));

        Subscription subscription = new Subscription();

        subscription.setBusiness(business);
        optionalTariff.ifPresent(subscription::setTariff);
        subscription.setActive(false);
        subscription.setStatusTariff(StatusTariff.WAITING);
        subscriptionRepository.save(subscription);


        AddressDto addressDto = businessDto.getAddressDto();
        BranchDto branchDto = businessDto.getBranchDto();
        UserDto userDto = businessDto.getUserDto();

        Address address = addressRepository.save(addressMapper.toEntity(addressDto));

        branchDto.setAddressId(address.getId());
        branchDto.setBusinessId(business.getId());
        Branch branch = branchRepository.save(branchMapper.toEntity(branchDto));
        List<UUID> branchIds = new ArrayList<>();
        branchIds.add(branch.getId());
        userDto.setBranchId(branchIds);

        Optional<Role> optionalRole = roleRepository.findByName("Admin");
        if (optionalRole.isPresent()) {
            Role roleAdmin = optionalRole.get();
            userDto.setRoleId(roleAdmin.getId());
        }
        userDto.setBusinessId(business.getId());

        userService.add(userDto, true);

        Optional<User> superadmin = userRepository.findByUsername("superadmin");

        if (superadmin.isPresent()) {
            Notification notification = new Notification();
            notification.setRead(false);
            notification.setName("Yangi bizness qo'shildi!");
            notification.setMessage("Yangi User va bizness qo'shildi biznes tarifini aktivlashtishingiz mumkin!");
            notification.setUserTo(superadmin.get());
            notification.setType(NotificationType.NEW_BUSINESS);
            notification.setObjectId(business.getId());
            notificationRepository.save(notification);
        }

        return new ApiResponse("ADDED", true);
    }

    public ApiResponse edit(UUID id, BusinessEditDto businessEditDto) {
        Optional<Business> optionalBusiness = businessRepository.findById(id);
        if (optionalBusiness.isEmpty()) return new ApiResponse("BUSINESS NOT FOUND", false);

        Optional<Business> businessOptional = businessRepository.findByName(businessEditDto.getName());
        if (businessOptional.isPresent()) {
            if (!businessOptional.get().getId().equals(id)) {
                return new ApiResponse("A BUSINESS WITH THAT NAME ALREADY EXISTS", false);
            }
        }

        Business business = optionalBusiness.get();
        business.setName(businessEditDto.getName());
        business.setDescription(businessEditDto.getDescription());
//        business.setSaleMinus(businessEditDto.isSaleMinus());
        business.setActive(businessEditDto.isActive());

        businessRepository.save(business);
        return new ApiResponse("EDITED", true);
    }

    public ApiResponse getOne(UUID id) {
        Optional<Business> optionalBusiness = businessRepository.findById(id);
        return optionalBusiness.map(business -> new ApiResponse("FOUND", true, business)).orElseGet(() -> new ApiResponse("not found business", false));
    }


    public ApiResponse getAllPartners() {
        Optional<Role> optionalRole = roleRepository.findByName(Constants.SUPERADMIN);
        if (optionalRole.isEmpty()) return new ApiResponse("NOT FOUND", false);
        Role superAdmin = optionalRole.get();

        Optional<Role> optionalAdmin = roleRepository.findByNameAndBusinessId(Constants.ADMIN, superAdmin.getBusiness().getId());
        if (optionalAdmin.isEmpty()) return new ApiResponse("NOT FOUND", false);
        Role admin = optionalAdmin.get();

        List<User> userList = userRepository.findAllByRole_IdAndBusiness_Delete(admin.getId(), false);
        if (userList.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, userList);
    }

    public ApiResponse deleteOne(UUID id) {
        Optional<Business> optionalBusiness = businessRepository.findById(id);
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("not found business", false);
        }
        Business business = optionalBusiness.get();
        business.setDelete(true);
        business.setActive(false);
        businessRepository.save(business);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getAll() {
        List<Business> all = businessRepository.findAllByDeleteIsFalse();
        return new ApiResponse("all business", true, businessMapper.toDtoList(all));
    }

    public ApiResponse deActive(UUID businessId) {
        Optional<Business> optionalBusiness = businessRepository.findById(businessId);
        if (optionalBusiness.isEmpty()) new ApiResponse("not found business", false);
        Business business = optionalBusiness.get();
        business.setActive(!business.isActive());
        businessRepository.save(business);
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse getInfo(String time) {
        // "day" ni doim qabul qiladi
        Timestamp startTime = Timestamp.valueOf(TODAY);
        if (time.equals("THIS_WEEK")) {
            startTime = Timestamp.valueOf(THIS_WEEK);
        } else if (time.equals("THIS_MONTH")) {
            startTime = Timestamp.valueOf(THIS_MONTH);
        } else if (time.equals("THIS_YEAR")) {
            startTime = Timestamp.valueOf(THIS_YEAR);
        }

        Integer subscribers = businessRepository.countAllByCreatedAtAfter(startTime);

        List<Subscription> subscriptionList = subscriptionRepository.findAllByCreatedAtAfterAndStatusTariff(startTime, StatusTariff.CONFIRMED);
        double subscriptionPayment = 0d;
        for (Subscription subscription : subscriptionList) {
            subscriptionPayment += subscription.getTariff().getPrice();
        }

        Integer waiting = subscriptionRepository.countAllByStatusTariff(StatusTariff.WAITING);

        Integer rejected = subscriptionRepository.countAllByStatusTariff(StatusTariff.REJECTED);

        SuperAdminInfoDto infoDto = new SuperAdminInfoDto(
                subscribers, rejected, waiting, subscriptionPayment
        );

        return new ApiResponse(true, infoDto);
    }

    public ApiResponse checkBusinessName(CheckDto checkDto) {
        boolean exists = businessRepository.existsByNameIgnoreCase(checkDto.getCheckName());
        if (exists) return new ApiResponse("EXIST", false);
        return new ApiResponse("NOT FOUND FOUND", true);
    }

    public ApiResponse checkUsername(CheckDto checkDto) {
        boolean exists = userRepository.existsByUsernameIgnoreCase(checkDto.getCheckName());
        if (exists) return new ApiResponse("EXIST", false);
        return new ApiResponse("NOT FOUND FOUND", true);
    }
}
