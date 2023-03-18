package uz.pdp.springsecurity.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.ExchangeStatusName.*;
import uz.pdp.springsecurity.enums.Permissions;
import uz.pdp.springsecurity.enums.StatusName;
import uz.pdp.springsecurity.enums.SuperAdmin;
import uz.pdp.springsecurity.repository.*;
import uz.pdp.springsecurity.util.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uz.pdp.springsecurity.enums.ExchangeStatusName.*;
import static uz.pdp.springsecurity.enums.Permissions.*;
import static uz.pdp.springsecurity.enums.StatusName.*;

@Component
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

    @Value("${spring.sql.init.mode}")
    private String initMode;

    @Override
    public void run(String... args) throws Exception {

        List<Business> allBusiness = businessRepository.findAll();
        Business business = null;
        if (allBusiness.isEmpty()) {
            business = new Business("Application", "Test Uchun");
            businessRepository.save(business);
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

        List<Branch> allBranch = branchRepository.findAll();
        Branch branch = null;
        if (allBranch.isEmpty()) {
            branch = new Branch(
                    "Test Filial",
                    address,
                    business,
                    1
            );
            branchRepository.save(branch);
        }
//------------------------------------------------------------------------------------------//

        if (initMode.equals("always")) {
            Permissions[] permissions = Permissions.values();
            SuperAdmin[] superAdmins = SuperAdmin.values();

            Role admin = roleRepository.save(new Role(Constants.ADMIN, Arrays.asList(permissions), business));

            Role superAdmin = roleRepository.save(new Role(Constants.SUPERADMIN, Arrays.asList(ADD_BUSINESS, EDIT_BUSINESS, VIEW_BUSINESS, DELETE_BUSINESS)));

            Role manager = roleRepository.save(new Role(
                    Constants.MANAGER,
                    Arrays.asList(
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
                            VIEW_TAX_ADMIN,

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
                            GET_BY_PRODUCT_TYPE,
                            UPDATE_PRODUCT_TYPE,
                            DELETE_PRODUCT_TYPE,
                            GET_PRODUCT_TYPE_PRODUCT_ID

                    ),
                    business
            ));

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
                            VIEW_PRODUCT

                    ),
                    business
            ));

            Set<Branch> branches = new HashSet<>();
            branches.add(branch);
            userRepository.save(new User(
                    "Admin",
                    "Admin",
                    "admin",
                    passwordEncoder.encode("123"),
                    admin,
                    true,
                    business,
                    branches
            ));

            userRepository.save(new User(
                    "SuperAdmin",
                    "Admin of site",
                    "superadmin",
                    passwordEncoder.encode("admin123"),
                    superAdmin,
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
                    branches
            ));

            userRepository.save(new User(
                    "Employee",
                    "employee",
                    "employee",
                    passwordEncoder.encode("employee123"),
                    employee,
                    true,
                    business,
                    branches
            ));

        }


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

        List<PaymentMethod> all1 = payMethodRepository.findAll();
        if (all1.isEmpty()) {
            payMethodRepository.save(new PaymentMethod(
                    "Naqd",
                    business
            ));

            payMethodRepository.save(new PaymentMethod(
                    "UzCard",
                    business
            ));

            payMethodRepository.save(new PaymentMethod(
                    "Humo",
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

//        List<CurrentCource> currentCourceList = currentCourceRepository.findAll();
//        if (currentCourceList.isEmpty()){
//            currentCourceRepository.save(new CurrentCource(110.0,currencyUSA,true));
//        }

    }

}