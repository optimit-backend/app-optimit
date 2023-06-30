package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.StatusName;
import uz.pdp.springsecurity.mapper.CustomerMapper;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerGroupRepository customerGroupRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final CustomerMapper mapper;
    private final TradeRepository tradeRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final BalanceService balanceService;
    private final RepaymentDebtRepository repaymentDebtRepository;
    private final CustomerDebtRepository customerDebtRepository;
    private final PayMethodRepository payMethodRepository;
    private final TradeProductRepository tradeProductRepository;

    public ApiResponse add(CustomerDto customerDto) {
        return createEdit(new Customer(), customerDto);
    }

    public ApiResponse edit(UUID id, CustomerDto customerDto) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        return optionalCustomer.map(customer -> createEdit(customer, customerDto)).orElseGet(() -> new ApiResponse("CUSTOMER NOT FOUND ", false));
    }

    private ApiResponse createEdit(Customer customer, CustomerDto customerDto) {
        List<Branch> branches = branchRepository.findAllById(customerDto.getBranches());
        if (branches.isEmpty())
            return new ApiResponse("BRANCH NOT FOUND", false);
        customer.setBranches(branches);

        if (customerDto.getCustomerGroupId() != null) {
            Optional<CustomerGroup> optionalCustomerGroup = customerGroupRepository.findById(customerDto.getCustomerGroupId());
            optionalCustomerGroup.ifPresent(customer::setCustomerGroup);
        } else {
            customer.setCustomerGroup(null);
        }

        customer.setName(customerDto.getName());
        customer.setPhoneNumber(customerDto.getPhoneNumber());
        customer.setTelegram(customerDto.getTelegram());
        customer.setBirthday(customerDto.getBirthday());
        customer.setDebt(customerDto.getDebt());
        customer.setPayDate(customerDto.getPayDate());
        customer.setLidCustomer(customerDto.getLidCustomer());
        customer.setDescription(customerDto.getDescription());

        customer.setBusiness(branches.get(0).getBusiness()); // TODO: 6/6/2023  delete
        customer.setBranch(branches.get(0)); // TODO: 6/6/2023  delete

        Customer save = customerRepository.save(customer);
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse get(UUID id) {
        Optional<Customer> optional = customerRepository.findById(id);
        if (optional.isEmpty()) {
            return new ApiResponse("NOT FOUND", true);
        }
        CustomerDto customerDto = mapper.toDto(optional.get());
        List<UUID> branches = new ArrayList<>();
        for (Branch branch : optional.get().getBranches()) {
            branches.add(branch.getId());
        }
        customerDto.setBranches(branches);
        return new ApiResponse("FOUND", true, customerDto);
    }

    public ApiResponse delete(UUID id) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        Customer customer = optionalCustomer.get();
        try {
            List<CustomerDebt> all = customerDebtRepository.findByCustomer_Id(id);
            for (CustomerDebt customerDebt : all) {
                customerDebt.setDelete(true);
                customerDebt.setCustomer(new Customer());
                customerDebtRepository.save(customerDebt);
            }
            List<RepaymentDebt> allByCustomerId = repaymentDebtRepository.findAllByCustomer_Id(id);
            for (RepaymentDebt repaymentDebt : allByCustomerId) {
                repaymentDebt.setDelete(true);
                repaymentDebt.setCustomer(new Customer());
                repaymentDebtRepository.save(repaymentDebt);
            }
            customerRepository.deleteById(id);
        } catch (Exception e) {
            return new ApiResponse("Qarzi bor yoki savdo qilgan mijozni o'chirib bo'lmaydi!", false);
        }
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        List<Customer> customerList = customerRepository.findAllByBusiness_Id(businessId);
        if (customerList.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, toCustomerDtoList(customerList));
    }

    private List<CustomerDto> toCustomerDtoList(List<Customer> customerList) {
        List<CustomerDto> customerDtoList = new ArrayList<>();
        for (Customer customer : customerList) {
            CustomerDto customerDto = mapper.toDto(customer);
            List<UUID> branches = new ArrayList<>();
            for (Branch branch : customer.getBranches()) {
                branches.add(branch.getId());
            }
            customerDto.setBranches(branches);
            customerDtoList.add(customerDto);
        }
        return customerDtoList;
    }

    public ApiResponse getAllByBranchId(UUID branchId) {
        List<Customer> customerList = customerRepository.findAllByBranchesId(branchId);
        if (customerList.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, toCustomerDtoList(customerList));
    }

    public ApiResponse repayment(UUID id, RepaymentDto repaymentDto) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) return new ApiResponse("CUSTOMER NOT FOUND", false);
        if (repaymentDto.getPayDate() == null) return new ApiResponse("PAY_DATE NOT FOUND", false);
        Customer customer = optionalCustomer.get();
        if (repaymentDto.getRepayment() != null) {
            customer.setDebt(customer.getDebt() - repaymentDto.getRepayment());
            customer.setPayDate(repaymentDto.getPayDate());
            customerRepository.save(customer);
            try {
                repaymentHelper(repaymentDto.getRepayment(), customer);
                balanceService.edit(customer.getBranch().getId(), repaymentDto.getRepayment(), true, repaymentDto.getPaymentMethodId());
                UUID paymentMethodId = repaymentDto.getPaymentMethodId();
                Optional<PaymentMethod> optionalPaymentMethod = payMethodRepository.findById(paymentMethodId);
                optionalPaymentMethod.ifPresent(paymentMethod -> repaymentDebtRepository.save(new RepaymentDebt(customer, repaymentDto.getRepayment(), paymentMethod, false)));
                return new ApiResponse("Repayment Customer !", true);
            } catch (Exception e) {
                return new ApiResponse("ERROR", false);
            }

        } else {
            return new ApiResponse("brat qarzingiz null kelyabdi !", false);
        }
    }

    private void repaymentHelper(double paidSum, Customer customer) {
        PaymentStatus tolangan = paymentStatusRepository.findByStatus(StatusName.TOLANGAN.name());
        PaymentStatus qisman_tolangan = paymentStatusRepository.findByStatus(StatusName.QISMAN_TOLANGAN.name());
        List<Trade> tradeList = tradeRepository.findAllByCustomerIdAndDebtSumIsNotOrderByCreatedAtAsc(customer.getId(), 0d);
        for (Trade trade : tradeList) {
            List<Payment> paymentList = paymentRepository.findAllByTradeId(trade.getId());
            Payment payment = paymentList.get(0);
            if (paidSum > trade.getDebtSum()) {
                paidSum -= trade.getDebtSum();
                trade.setDebtSum(0);
                trade.setPaidSum(trade.getTotalSum());
                trade.setPaymentStatus(tolangan);
                payment.setPaidSum(payment.getPaidSum() + trade.getDebtSum());
                paymentRepository.save(payment);
            } else if (paidSum == trade.getDebtSum()) {
                trade.setDebtSum(0);
                trade.setPaidSum(trade.getTotalSum());
                trade.setPaymentStatus(tolangan);
                payment.setPaidSum(payment.getPaidSum() + trade.getDebtSum());
                paymentRepository.save(payment);
                break;
            } else {
                trade.setDebtSum(trade.getDebtSum() - paidSum);
                trade.setPaidSum(trade.getPaidSum() + paidSum);
                trade.setPaymentStatus(qisman_tolangan);
                payment.setPaidSum(payment.getPaidSum() + paidSum);
                paymentRepository.save(payment);
                break;
            }
        }
        tradeRepository.saveAll(tradeList);
    }

    public ApiResponse getAllByGroupId(UUID groupId) {
        List<Customer> customerList = customerRepository.findAllByCustomerGroupId(groupId);
        if (customerList.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        return new ApiResponse("all customers", true, toCustomerDtoList(customerList));
    }

    public ApiResponse getAllByLidCustomer(UUID branchId) {
        List<Customer> customerList = customerRepository.findAllByBranchesIdAndLidCustomerIsTrue(branchId);
        if (customerList.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        List<CustomerDto> customerDtoList = toCustomerDtoList(customerList);
        List<Map<String, Object>> responses = new ArrayList<>();
        double totalTrade = 0;

        for (CustomerDto customerDto : customerDtoList) {
            Map<String, Object> response = new HashMap<>();
            List<Trade> allByCustomerId = tradeRepository.findAllByCustomer_Id(customerDto.getId());
            double totalSumma = 0;
            double profit = 0;
            for (Trade trade : allByCustomerId) {
                totalSumma += trade.getTotalSum();
                profit += trade.getTotalProfit();
            }
            totalTrade += totalSumma;
            response.put("customer", customerDto);
            response.put("totalSumma", totalSumma);
            response.put("size", customerList.size());
            response.put("totalTrade", totalTrade);
            response.put("profit", profit);
            responses.add(response);
        }
        return new ApiResponse("found", true, responses);
    }

    public ApiResponse getCustomerInfo(UUID customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            return new ApiResponse("not found customer", false);
        }

        CustomerInfoDto customerInfoDto = new CustomerInfoDto();
        Customer customer = optionalCustomer.get();
        CustomerDto customerDto = mapper.toDto(customer);
        customerInfoDto.setCustomerDto(customerDto);

        Double totalSumByCustomer = tradeRepository.totalSumByCustomer(customerId);
        Double totalProfitByCustomer = tradeRepository.totalProfitByCustomer(customerId);
        customerInfoDto.setTotalTradeSum(totalSumByCustomer != null ? totalSumByCustomer : 0);
        customerInfoDto.setTotalProfitSum(totalProfitByCustomer != null ? totalProfitByCustomer : 0);

        return new ApiResponse("data", true, customerInfoDto);
    }

    public ApiResponse getCustomerTradeInfo(UUID customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            return new ApiResponse("not found customer", false);
        }

        List<CustomerTradeInfo> customerTradeInfo = new ArrayList<>();
        List<Trade> all = tradeRepository.findAllByCustomer_Id(customerId);
        for (Trade trade : all) {
            CustomerTradeInfo customerTradeInfo1 = new CustomerTradeInfo();
            List<TradeProductCustomerDto> tradeProductCustomerDtoList = new ArrayList<>();

            List<TradeProduct> allByTradeId = tradeProductRepository.findAllByTradeId(trade.getId());

            for (TradeProduct tradeProduct : allByTradeId) {
                TradeProductCustomerDto productCustomerDto = new TradeProductCustomerDto();
                if (tradeProduct.getBacking() != null) {
                    customerTradeInfo1.setTrade(false);
                    customerTradeInfo1.setTotalSumma(tradeProduct.getBacking());
                } else {
                    customerTradeInfo1.setTrade(true);
                    customerTradeInfo1.setTotalSumma(tradeProduct.getTrade().getTotalSum());
                }
                if (tradeProduct.getProduct() != null) {
                    if (tradeProduct.getProduct().getPhoto() != null) {
                        productCustomerDto.setAttachmentId(tradeProduct.getProduct().getPhoto().getId());
                    }
                }
                if (tradeProduct.getProductTypePrice() != null) {
                    if (tradeProduct.getProductTypePrice().getPhoto() != null) {
                        productCustomerDto.setAttachmentId(tradeProduct.getProductTypePrice().getPhoto().getId());
                    }
                }

                productCustomerDto.setProductName(tradeProduct.getProductTypePrice() != null ?
                        tradeProduct.getProductTypePrice().getName() : tradeProduct.getProduct().getName());

                tradeProductCustomerDtoList.add(productCustomerDto);
            }

            customerTradeInfo1.setCreateAt(trade.getCreatedAt());
            customerTradeInfo1.setProductCutomerDtoList(tradeProductCustomerDtoList);
            customerTradeInfo.add(customerTradeInfo1);
        }

        if (customerTradeInfo.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        return new ApiResponse("all", true, customerTradeInfo);
    }

    public ApiResponse getCustomerPreventedInfo(UUID customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            return new ApiResponse("not found customer", false);
        }

        List<Trade> all = tradeRepository.findAllByCustomer_Id(customerId);
        List<CustomerPreventedInfoDto> customerPreventedInfoDtoList = new ArrayList<>();

        for (Trade trade : all) {
            CustomerPreventedInfoDto customerPreventedInfoDto = new CustomerPreventedInfoDto();
            Double paidSum = tradeRepository.totalPaidSum(trade.getId());
            TotalPaidSumDto totalPaidSumDto = new TotalPaidSumDto();
            totalPaidSumDto.setCreateAt(trade.getCreatedAt());
            totalPaidSumDto.setPaidSum(paidSum != null ? paidSum : 0);
            totalPaidSumDto.setPayMethodName(trade.getPayMethod().getType());
            customerPreventedInfoDto.setTotalPaidSumDto(totalPaidSumDto);

            if (trade.getDebtSum() != 0) {
                customerPreventedInfoDto.setDebtSum(trade.getDebtSum());
            }

            List<TradeProduct> allByTradeId = tradeProductRepository.findAllByTradeId(trade.getId());
            for (TradeProduct tradeProduct : allByTradeId) {
                BackingProductDto backingProductDto = new BackingProductDto();
                if (tradeProduct.getBacking() != null) {
                    backingProductDto.setCreateAt(tradeProduct.getCreatedAt());
                    backingProductDto.setPaidSum(tradeProduct.getBacking());
                    backingProductDto.setPayMethodName(tradeProduct.getTrade().getPayMethod().getType());
                } else {
                    backingProductDto.setPaidSum(0.0);
                }
                customerPreventedInfoDto.setBackingProductDto(backingProductDto);
            }
            customerPreventedInfoDtoList.add(customerPreventedInfoDto);
            Customer customer = optionalCustomer.get();
            customerPreventedInfoDto.setBalance(customer.getDebt());
        }


        if (customerPreventedInfoDtoList.isEmpty()) {
            return new ApiResponse("not found", false);
        }

        return new ApiResponse("found", true, customerPreventedInfoDtoList);
    }
}
