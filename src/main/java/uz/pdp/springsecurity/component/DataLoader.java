package uz.pdp.springsecurity.component;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.entity.Currency;
import uz.pdp.springsecurity.enums.*;
import uz.pdp.springsecurity.repository.*;
import uz.pdp.springsecurity.util.Constants;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

import static uz.pdp.springsecurity.enums.ExchangeStatusName.*;
import static uz.pdp.springsecurity.enums.Permissions.*;
import static uz.pdp.springsecurity.enums.StatusName.*;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PaymentStatusRepository paymentStatusRepository;

    @Autowired
    PayMethodRepository payMethodRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CurrentCourceRepository currentCourceRepository;

    @Autowired
    ExchangeStatusRepository exchangeStatusRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    AttachmentContentRepository attachmentContentRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TariffRepository tariffRepository;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    private final MeasurementRepository measurementRepository;

    @Value("${spring.sql.init.mode}")
    private String initMode;

    @Override
    public void run(String... args) throws Exception {
//------------------------------------------------------------------------------------------//
        if (initMode.equals("always")) {
            List<Tariff> tariffRepositoryAll = tariffRepository.findAll();
            Tariff tariff = null;
            if (tariffRepositoryAll.isEmpty()) {
                tariff = new Tariff(
                        "test tariff",
                        "test uchun",
                        0,
                        0,
                        0,
                        0,
                        Lifetime.MONTH,
                        0,
                        1,
                        100,
                        0,
                        true,
                        false
                );
                tariffRepository.save(tariff);
            }

            List<Business> allBusiness = businessRepository.findAll();
            Business business = null;
            if (allBusiness.isEmpty()) {
                business = new Business();
                business.setDescription("Test Uchun");
                business.setName("Application");
                business.setActive(true);
                business.setDelete(false);
                business = businessRepository.save(business);
            }

            if (business != null) {
                List<Measurement> measurementList = measurementRepository.findAllByBusiness_Id(business.getId());
                if (measurementList.isEmpty()) {
                    measurementRepository.save(
                            new Measurement("dona",
                                    business)
                    );
                }
            }

            if (business != null) {
                List<Brand> allByBusinessId = brandRepository.findAllByBusiness_Id(business.getId());
                if (allByBusinessId.isEmpty()) {
                    brandRepository.save(
                            new Brand("brand",
                                    business)
                    );
                }
            }

            if (business != null) {
                List<Category> allByBusinessId = categoryRepository.findAllByBusiness_Id(business.getId());
                if (allByBusinessId.isEmpty()) {
                    categoryRepository.save(
                            new Category("category",

                                    business)
                    );
                }
            }

            if (business != null) {
                Optional<Subscription> subscriptionOptional = subscriptionRepository.findByBusinessIdAndActiveTrue(business.getId());
                if (subscriptionOptional.isEmpty()) {
                    Timestamp startDay = new Timestamp(System.currentTimeMillis());
                    LocalDate date = LocalDate.now().plusMonths(1);
                    Timestamp endDay = Timestamp.valueOf(date.atStartOfDay());

                    Subscription subscription = new Subscription(
                            business,
                            tariff,
                            startDay,
                            endDay,
                            StatusTariff.CONFIRMED,
                            PayType.OFFLINE,
                            true,
                            true,
                            false
                    );
                    subscriptionRepository.save(subscription);
                }
            }
//------------------------------------------------------------------------------------------//
            List<Address> addresses = addressRepository.findAll();
            Address address = null;
            if (addresses.isEmpty()) {
                address = new Address(
                        "Tashkent",
                        "Shayxontuxur",
                        "Gulobod",
                        "1"
                );
                addressRepository.save(address);
            }
//------------------------------------------------------------------------------------------//

            Permissions[] permissions = Permissions.values();

            Role superAdmin = roleRepository.save(new Role(
                    Constants.SUPERADMIN,
                    List.of(permissions),
                    business
            ));


            Role admin = roleRepository.save(
                    new Role(
                            Constants.ADMIN,
                            Arrays.asList(
                                    GET_TARIFF,

                                    ADD_ADDRESS,
                                    EDIT_ADDRESS,
                                    VIEW_ADDRESS,
                                    DELETE_ADDRESS,

                                    UPLOAD_MEDIA,
                                    DOWNLOAD_MEDIA,
                                    VIEW_MEDIA_INFO,
                                    DELETE_MEDIA,

                                    ADD_BRANCH,
                                    EDIT_BRANCH,
                                    VIEW_BRANCH_ADMIN,
                                    VIEW_BRANCH,
                                    DELETE_BRANCH,

                                    ADD_BRAND,
                                    EDIT_BRAND,
                                    VIEW_BRAND,
                                    DELETE_BRAND,

                                    ADD_CATEGORY,
                                    EDIT_CATEGORY,
                                    VIEW_CATEGORY,
                                    VIEW_CATEGORY_ADMIN,
                                    DELETE_CATEGORY,
                                    ADD_CHILD_CATEGORY,

                                    ADD_CURRENCY,
                                    EDIT_CURRENCY,
                                    VIEW_CURRENCY,
                                    DELETE_CURRENCY,

                                    ADD_CUSTOMER,
                                    EDIT_CUSTOMER,
                                    VIEW_CUSTOMER,
                                    VIEW_CUSTOMER_ADMIN,
                                    DELETE_CUSTOMER,

                                    ADD_MEASUREMENT,
                                    EDIT_MEASUREMENT,
                                    VIEW_MEASUREMENT,
                                    VIEW_MEASUREMENT_ADMIN,
                                    DELETE_MEASUREMENT,

                                    ADD_OUTLAY,
                                    EDIT_OUTLAY,
                                    VIEW_OUTLAY,
                                    VIEW_OUTLAY_ADMIN,
                                    DELETE_OUTLAY,

                                    ADD_PRODUCT,
                                    EDIT_PRODUCT,
                                    VIEW_PRODUCT,

                                    VIEW_PRODUCT_ADMIN,
                                    DELETE_PRODUCT,

                                    ADD_ROLE,
                                    EDIT_ROLE,
                                    VIEW_ROLE,
                                    VIEW_ROLE_ADMIN,
                                    DELETE_ROLE,

                                    ADD_SUPPLIER,
                                    EDIT_SUPPLIER,
                                    VIEW_SUPPLIER,
                                    VIEW_SUPPLIER_ADMIN,
                                    DELETE_SUPPLIER,

                                    ADD_USER,
                                    EDIT_USER,
                                    VIEW_USER,
                                    VIEW_USER_ADMIN,
                                    DELETE_USER,
                                    EDIT_MY_PROFILE,

                                    ADD_TRADE,
                                    EDIT_TRADE,
                                    VIEW_TRADE,
                                    VIEW_TRADE_ADMIN,
                                    DELETE_TRADE,
                                    DELETE_MY_TRADE,
                                    VIEW_MY_TRADE,

                                    ADD_PAY_METHOD,
                                    EDIT_PAY_METHOD,
                                    VIEW_PAY_METHOD,
                                    VIEW_PAY_METHOD_ADMIN,
                                    DELETE_PAY_METHOD,

                                    ADD_PAY_STATUS,
                                    EDIT_PAY_STATUS,
                                    VIEW_PAY_STATUS,
                                    VIEW_PAY_STATUS_ADMIN,
                                    DELETE_PAY_STATUS,

                                    ADD_PURCHASE,
                                    EDIT_PURCHASE,
                                    VIEW_PURCHASE,
                                    VIEW_PURCHASE_ADMIN,
                                    DELETE_PURCHASE,

                                    ADD_EXCHANGE,
                                    EDIT_EXCHANGE,
                                    VIEW_EXCHANGE,
                                    VIEW_EXCHANGE_ADMIN,
                                    DELETE_EXCHANGE,

                                    VIEW_BENEFIT_AND_LOST,

                                    ADD_CUSTOMER_GROUP,
                                    DELETE_CUSTOMER_GROUP,
                                    EDIT_CUSTOMER_GROUP,
                                    VIEW_CUSTOMER_GROUP,

                                    ADD_TAX,
                                    DELETE_TAX,
                                    EDIT_TAX,
                                    VIEW_TAX,
                                    ADD_PRODUCT_TYPE,
                                    GET_PRODUCT_TYPE,

                                    UPDATE_PRODUCT_TYPE,
                                    DELETE_PRODUCT_TYPE,

                                    GET_EXCEL,
                                    POST_EXCEL,

                                    VIEW_INFO,


                                    CREATE_CONTENT,
                                    EDIT_CONTENT,
                                    GET_CONTENT,
                                    DELETE_CONTENT,

                                    CREATE_PRODUCTION,
                                    GET_PRODUCTION,
                                    VIEW_REPORT,

                                    GET_COURSE
                            ),
                            business));
            Role manager = roleRepository.save(new Role(
                    Constants.MANAGER,
                    Arrays.asList(
                            GET_TARIFF,

                            ADD_ADDRESS,
                            EDIT_ADDRESS,
                            VIEW_ADDRESS,
                            DELETE_ADDRESS,

                            UPLOAD_MEDIA,
                            DOWNLOAD_MEDIA,
                            VIEW_MEDIA_INFO,
                            DELETE_MEDIA,

                            ADD_BRAND,
                            EDIT_BRAND,
                            VIEW_BRAND,
                            DELETE_BRAND,

                            ADD_CATEGORY,
                            EDIT_CATEGORY,
                            VIEW_CATEGORY,
                            DELETE_CATEGORY,
                            ADD_CHILD_CATEGORY,

                            ADD_CURRENCY,
                            EDIT_CURRENCY,
                            VIEW_CURRENCY,
                            DELETE_CURRENCY,

                            ADD_CUSTOMER,
                            EDIT_CUSTOMER,
                            VIEW_CUSTOMER,
                            DELETE_CUSTOMER,

                            ADD_MEASUREMENT,
                            EDIT_MEASUREMENT,
                            VIEW_MEASUREMENT,
                            DELETE_MEASUREMENT,

                            ADD_OUTLAY,
                            EDIT_OUTLAY,
                            VIEW_OUTLAY,
                            DELETE_OUTLAY,

                            ADD_PRODUCT,
                            EDIT_PRODUCT,
                            VIEW_PRODUCT,
                            DELETE_PRODUCT,
                            VIEW_PRODUCT_ADMIN,

                            ADD_ROLE,
                            EDIT_ROLE,
                            VIEW_ROLE,
                            DELETE_ROLE,

                            ADD_SUPPLIER,
                            EDIT_SUPPLIER,
                            VIEW_SUPPLIER,
                            DELETE_SUPPLIER,

                            ADD_USER,
                            EDIT_USER,
                            VIEW_USER,
                            DELETE_USER,
                            EDIT_MY_PROFILE,

                            ADD_TRADE,
                            EDIT_TRADE,
                            VIEW_TRADE,
                            DELETE_TRADE,
                            DELETE_MY_TRADE,
                            VIEW_MY_TRADE,

                            ADD_TAX,
                            DELETE_TAX,
                            EDIT_TAX,
                            VIEW_TAX,

                            ADD_CUSTOMER_GROUP,
                            DELETE_CUSTOMER_GROUP,
                            EDIT_CUSTOMER_GROUP,
                            VIEW_CUSTOMER_GROUP,

                            ADD_PAY_METHOD,
                            EDIT_PAY_METHOD,
                            VIEW_PAY_METHOD,
                            DELETE_PAY_METHOD,

                            ADD_PAY_STATUS,
                            EDIT_PAY_STATUS,
                            VIEW_PAY_STATUS,
                            DELETE_PAY_STATUS,

                            ADD_PURCHASE,
                            EDIT_PURCHASE,
                            VIEW_PURCHASE,
                            DELETE_PURCHASE,

                            ADD_EXCHANGE,
                            EDIT_EXCHANGE,
                            VIEW_EXCHANGE,
                            DELETE_EXCHANGE,

                            VIEW_BENEFIT_AND_LOST,

                            ADD_PRODUCT_TYPE,
                            GET_PRODUCT_TYPE,
                            UPDATE_PRODUCT_TYPE,
                            DELETE_PRODUCT_TYPE,

                            GET_EXCEL,
                            POST_EXCEL,

                            VIEW_INFO,

                            GET_BUSINESS_ALL_AMOUNT,


                            CREATE_CONTENT,
                            EDIT_CONTENT,
                            GET_CONTENT,
                            DELETE_CONTENT,

                            CREATE_PRODUCTION,
                            GET_PRODUCTION,
                            VIEW_REPORT),
                    business));

            Role employee = roleRepository.save(new Role(
                    Constants.EMPLOYEE,
                    Arrays.asList(UPLOAD_MEDIA,
                            DOWNLOAD_MEDIA,
                            VIEW_MEDIA_INFO,
                            VIEW_BRAND,
                            ADD_CURRENCY,
                            EDIT_CURRENCY,
                            VIEW_CURRENCY,
                            DELETE_CURRENCY,
                            ADD_MEASUREMENT,
                            EDIT_MEASUREMENT,
                            VIEW_MEASUREMENT,
                            DELETE_MEASUREMENT,

                            ADD_TRADE,
                            EDIT_TRADE,
                            VIEW_MY_TRADE,
                            DELETE_MY_TRADE,

                            ADD_PAY_METHOD,
                            EDIT_PAY_METHOD,
                            VIEW_PAY_METHOD,
                            DELETE_PAY_METHOD,
                            ADD_PAY_STATUS,
                            EDIT_PAY_STATUS,
                            VIEW_PAY_STATUS,
                            DELETE_PAY_STATUS,
                            EDIT_MY_PROFILE,
                            VIEW_PRODUCT,

                            CREATE_CONTENT,
                            EDIT_CONTENT,
                            GET_CONTENT,
                            DELETE_CONTENT,

                            CREATE_PRODUCTION,
                            GET_PRODUCTION

                    ),
                    business
            ));

            List<PaymentMethod> all1 = payMethodRepository.findAll();
            if (all1.isEmpty()) {
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
            }

            List<Currency> currencyList = currencyRepository.findAll();
            Currency currencyUSA = new Currency();
            if (currencyList.isEmpty()) {
//            currencyUSA = currencyRepository.save(new Currency(
//                    "DOLLAR",
//                    "USA",
//                    business,
//                    true
//            ));

                Currency currencyUZB = currencyRepository.save(new Currency(
                        "SO'M",
                        "UZB",
                        business,
                        true
                ));
            }

            Set<Branch> branches = new HashSet<>();
            if (business != null) {
                List<Branch> allBranch = branchRepository.findAllByBusiness_Id(business.getId());
                Branch branch = null;
                if (allBranch.isEmpty()) {
                    branch = new Branch(
                            "Test Filial",
                            address,
                            business
                    );
                    branchRepository.save(branch);
                    branches.add(branch);
                }
            }

            userRepository.save(new User(
                    "Admin",
                    "Admin",
                    "admin",
                    passwordEncoder.encode("123"),
                    admin,
                    true,
                    business,
                    branches,
                    true
            ));

            userRepository.save(new User(
                    "SuperAdmin",
                    "Admin of site",
                    "superadmin",
                    passwordEncoder.encode("admin123"),
                    superAdmin,
                    true,
                    business,
                    branches,
                    true
            ));
            userRepository.save(new User(
                    "Manager",
                    "manager",
                    "manager",
                    passwordEncoder.encode("manager123"),
                    manager,
                    true,
                    business,
                    branches,
                    true
            ));

            userRepository.save(new User(
                    "Employee",
                    "employee",
                    "employee",
                    passwordEncoder.encode("employee123"),
                    employee,
                    true,
                    business,
                    branches,
                    true
            ));

            List<PaymentStatus> all = paymentStatusRepository.findAll();
            if (all.isEmpty()) {
                paymentStatusRepository.save(new PaymentStatus(
                        TOLANGAN.name()
                ));

                paymentStatusRepository.save(new PaymentStatus(
                        QISMAN_TOLANGAN.name()
                ));

                paymentStatusRepository.save(new PaymentStatus(
                        TOLANMAGAN.name()
                ));
            }

            List<ExchangeStatus> exchangeStatusRepositoryAll = exchangeStatusRepository.findAll();
            if (exchangeStatusRepositoryAll.isEmpty()) {
                exchangeStatusRepository.save(new ExchangeStatus(
                        BUYURTMA_QILINGAN.name()
                ));

                exchangeStatusRepository.save(new ExchangeStatus(
                        KUTILMOQDA.name()
                ));

                exchangeStatusRepository.save(new ExchangeStatus(
                        QABUL_QILINGAN.name()
                ));
            }
        }
    }
}
