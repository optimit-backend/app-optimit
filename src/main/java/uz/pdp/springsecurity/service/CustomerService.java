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

        customer.setBusiness(branches.get(0).getBusiness()); // TODO: 6/6/2023  delete
        customer.setBranch(branches.get(0)); // TODO: 6/6/2023  delete

        customerRepository.save(customer);
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
        if (!customerRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);
        customerRepository.deleteById(id);
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
                repaymentDebtRepository.save(new RepaymentDebt(customer, repaymentDto.getRepayment()));
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
}
