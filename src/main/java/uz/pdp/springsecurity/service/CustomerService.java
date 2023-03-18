package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Branch;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.Customer;
import uz.pdp.springsecurity.entity.CustomerGroup;
import uz.pdp.springsecurity.mapper.CustomerMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.CustomerDto;
import uz.pdp.springsecurity.payload.RepaymentDto;
import uz.pdp.springsecurity.repository.BranchRepository;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.CustomerGroupRepository;
import uz.pdp.springsecurity.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public ApiResponse add(CustomerDto customerDto) {

        Optional<Business> optionalBusiness = businessRepository.findById(customerDto.getBusinessId());
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("BUSINESS NOT FOUND", false);
        }

        Optional<Branch> optionalBranch = branchRepository.findById(customerDto.getBranchId());
        if (optionalBranch.isEmpty()) {
            return new ApiResponse("BRANCH NOT FOUND", false);
        }

        Optional<CustomerGroup> customerGroupOptional = customerGroupRepository.findById(customerDto.getCustomerGroupId());
        if (customerGroupOptional.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }


        Customer customer = mapper.toEntity(customerDto);

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

        Optional<CustomerGroup> optionalCustomerGroup = customerGroupRepository.findById(customerDto.getCustomerGroupId());
        if (optionalCustomerGroup.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }

        Customer customer = optionalCustomer.get();
        mapper.update(customerDto, customer);
        customerRepository.save(customer);


        return new ApiResponse("EDITED", true);
    }

    public ApiResponse get(UUID id) {
        Optional<Customer> optional = customerRepository.findById(id);
        if (optional.isEmpty()) {
            return new ApiResponse("NOT FOUND", true);
        }
        Customer customer = optional.get();

        return new ApiResponse("FOUND", true, mapper.toDto(customer));
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
        try {
            Optional<Customer> optionalCustomer = customerRepository.findById(id);
            if (optionalCustomer.isEmpty()) return new ApiResponse("CUSTOMER NOT FOUND", false);
            Customer customer = optionalCustomer.get();

            if (repaymentDto.getRepayment() != null && customer.getDebt() != null) {
                customer.setDebt(customer.getDebt() - repaymentDto.getRepayment());
                customerRepository.save(customer);
                return new ApiResponse("Repayment Customer !", true);
            } else {
                return new ApiResponse("brat qarzingiz null kelyabdi !", false);
            }
        } catch (Exception e) {
            return new ApiResponse("Exception Xatolik !", false);
        }

    }


}
