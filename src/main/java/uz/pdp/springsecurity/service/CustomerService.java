package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    CustomerGroupRepository customerGroupRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    BranchRepository branchRepository;

    private final CustomerMapper mapper;
    private final TradeRepository tradeRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final BalanceService balanceService;

    public ApiResponse add(CustomerDto customerDto) {

        Optional<Business> optionalBusiness = businessRepository.findById(customerDto.getBusinessId());
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("BUSINESS NOT FOUND", false);
        }

        Optional<Branch> optionalBranch = branchRepository.findById(customerDto.getBranchId());
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("BRANCH NOT FOUND", false);
        }

        Customer customer = mapper.toEntity(customerDto);
        if (customerDto.getCustomerGroupId() != null) {
            Optional<CustomerGroup> customerGroupOptional = customerGroupRepository.findById(customerDto.getCustomerGroupId());
            customerGroupOptional.ifPresent(customer::setCustomerGroup);
        }
        customer.setLidCustomer(customerDto.getLidCustomer());
        customerRepository.save(customer);
        return new ApiResponse("ADDED", true);
    }

    public ApiResponse edit(UUID id, CustomerDto customerDto) {

        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) {
            return new ApiResponse("NOT FOUND ", false);
        }

        Optional<Business> optionalBusiness = businessRepository.findById(customerDto.getBusinessId());
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("BRANCH NOT FOUND", false);
        }

        Optional<Branch> optionalBranch = branchRepository.findById(customerDto.getBranchId());
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("BRANCH NOT FOUND", false);
        }
        CustomerGroup customerGroup = null;
        if (customerDto.getCustomerGroupId() != null) {
            Optional<CustomerGroup> optionalCustomerGroup = customerGroupRepository.findById(customerDto.getCustomerGroupId());
            if (optionalCustomerGroup.isPresent()) {
                customerGroup = optionalCustomerGroup.get();
            }

        }
        Customer customer = optionalCustomer.get();

        customer.setName(customerDto.getName());
        customer.setPayDate(customerDto.getPayDate());
        customer.setPhoneNumber(customerDto.getPhoneNumber());
        customer.setTelegram(customerDto.getTelegram());
        customer.setDebt(customerDto.getDebt());
        if (customerGroup != null) {
            customer.setCustomerGroup(customerGroup);
        }
        customer.setBusiness(optionalBusiness.get());
        customer.setBranch(optionalBranch.get());

        customerRepository.save(customer);

        return new ApiResponse("EDITED", true);
    }

    public ApiResponse get(UUID id) {
        Optional<Customer> optional = customerRepository.findById(id);
        if (optional.isEmpty()) {
            return new ApiResponse("NOT FOUND", true);
        }
        Customer customer = optional.get();
        CustomerDto customerDto = mapper.toDto(customer);
        if (customer.getCustomerGroup() != null) {
            customerDto.setCustomerGroupId(customer.getCustomerGroup().getId());
        }
        return new ApiResponse("FOUND", true, customerDto);
    }

    public ApiResponse delete(UUID id) {
        if (!customerRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);
        customerRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        List<Customer> allByBusinessId = customerRepository.findAllByBusiness_Id(businessId);
        if (allByBusinessId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, mapper.toDtoList(allByBusinessId));
    }

    public ApiResponse getAllByBranchId(UUID branchId) {
        List<Customer> allByBranchId = customerRepository.findAllByBranchId(branchId);
        if (allByBranchId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, mapper.toDtoList(allByBranchId));
    }

    public ApiResponse repayment(UUID id, RepaymentDto repaymentDto) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) return new ApiResponse("CUSTOMER NOT FOUND", false);
        if (repaymentDto.getPayDate() == null) return new ApiResponse("PAY_DATE NOT FOUND", false);
        Customer customer = optionalCustomer.get();
        if (repaymentDto.getRepayment() != null) {
            if (repaymentDto.getRepayment() < 0) {
                customer.setDebt(customer.getDebt() + repaymentDto.getRepayment());
            } else {
                if (customer.getDebt() < 0) {
                    customer.setDebt(customer.getDebt() + repaymentDto.getRepayment());
                } else {
                    customer.setDebt(customer.getDebt() - repaymentDto.getRepayment());
                }
            }
            customer.setPayDate(repaymentDto.getPayDate());
            customerRepository.save(customer);
            try {
                repaymentHelper(repaymentDto.getRepayment(), customer);
                balanceService.edit(customer.getBranch().getId(), repaymentDto.getRepayment(), true, repaymentDto.getPaymentMethodId());
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
        List<Customer> allCustomer = customerRepository.findAllByCustomerGroupId(groupId);
        if (allCustomer.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        return new ApiResponse("all customers", true, mapper.toDtoList(allCustomer));
    }

    public ApiResponse getAllByLidCustomer(UUID branchId) {
        List<Customer> all = customerRepository.findAllByBranchIdAndLidCustomerIsTrue(branchId);
        if (all.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        List<CustomerDto> customerDtoList = mapper.toDtoList(all);
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
            response.put("size", all.size());
            response.put("totalTrade", totalTrade);
            response.put("profit", profit);
            responses.add(response);
        }


        return new ApiResponse("found", true, responses);
    }
}
