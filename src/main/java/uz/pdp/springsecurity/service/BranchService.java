package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.BalanceType;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.BranchDto;
import uz.pdp.springsecurity.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchService {
    @Autowired
    BranchRepository branchRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    TaskStatusRepository taskStatusRepository;

    @Autowired
    ProjectStatusRepository projectStatusRepository;

    @Autowired
    BusinessRepository businessRepository;
    private final InvoiceService invoiceService;
    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final PayMethodRepository payMethodRepository;

    public ApiResponse addBranch(BranchDto branchDto) {
        Branch branch = new Branch();

        branch.setName(branchDto.getName());

        Optional<Address> byId = addressRepository.findById(branchDto.getAddressId());
        if (byId.isEmpty()) return new ApiResponse("ADDRESS NOT FOUND", false);
        branch.setAddress(byId.get());

        Optional<Business> optionalBusiness = businessRepository.findById(branchDto.getBusinessId());
        if (optionalBusiness.isEmpty()) return new ApiResponse("BUSINESS NOT FOUND", false);
        branch.setBusiness(optionalBusiness.get());

        branchRepository.save(branch);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user.getBranches().add(branch);
        userRepository.save(user);
        invoiceService.create(branch);


        ProjectStatus projectStatus = new ProjectStatus();
        projectStatus.setName("Uncompleted");
        projectStatus.setColor("red");
        projectStatus.setBranch(branch);
        projectStatusRepository.save(projectStatus);

        ProjectStatus projectStatus2 = new ProjectStatus();
        projectStatus2.setColor("yellow");
        projectStatus2.setName("Process");
        projectStatus2.setBranch(branch);
        projectStatusRepository.save(projectStatus2);

        ProjectStatus projectStatus3 = new ProjectStatus();
        projectStatus3.setColor("green");
        projectStatus3.setName("Completed");
        projectStatus3.setBranch(branch);
        projectStatusRepository.save(projectStatus3);


        TaskStatus completedTaskStatus = new TaskStatus();
        completedTaskStatus.setName("Completed");
        completedTaskStatus.setOrginalName("Completed");
        completedTaskStatus.setRowNumber(2);
        completedTaskStatus.setABoolean(true);
        completedTaskStatus.setColor("#04d227");
        completedTaskStatus.setBranch(branch);
        taskStatusRepository.save(completedTaskStatus);

        TaskStatus uncompletedTaskStatus = new TaskStatus();
        uncompletedTaskStatus.setName("Uncompleted");
        uncompletedTaskStatus.setOrginalName("Uncompleted");
        uncompletedTaskStatus.setRowNumber(1);
        uncompletedTaskStatus.setABoolean(true);
        uncompletedTaskStatus.setColor("#FF0000");
        uncompletedTaskStatus.setBranch(branch);
        taskStatusRepository.save(uncompletedTaskStatus);
    }

    static void createBalance(Branch branch, BalanceRepository balanceRepository, PayMethodRepository payMethodRepository) {
        List<PaymentMethod> allByBusinessId = payMethodRepository.findAllByBusiness_Id(branch.getBusiness().getId());

        for (PaymentMethod paymentMethod : allByBusinessId) {
            Balance balance = new Balance();
            balance.setAccountSumma(0);
            balance.setBranch(branch);
            balance.setPaymentMethod(paymentMethod);
            balanceRepository.save(balance);
        }
    }

    public ApiResponse editBranch(UUID id, BranchDto branchDto) {
        if (!branchRepository.existsById(id)) return new ApiResponse("BRANCH NOT FOUND", false);

        Branch branch = branchRepository.getById(id);
        branch.setName(branchDto.getName());

        if (!addressRepository.existsById(branchDto.getAddressId())) return new ApiResponse("ADDRESS NOT FOUND", false);
        branch.setAddress(branch.getAddress());

        Optional<Business> optionalBusiness = businessRepository.findById(branchDto.getBusinessId());
        if (optionalBusiness.isEmpty()) return new ApiResponse("BUSINESS NOT FOUND", false);
        branch.setBusiness(optionalBusiness.get());
        branchRepository.save(branch);
        return new ApiResponse("EDITED", true);
    }

    public ApiResponse getBranch(UUID id) {
        if (!branchRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, branchRepository.findById(id).get());
    }

    public ApiResponse deleteBranch(UUID id) {
        if (!branchRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);

        branchRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getByBusinessId(UUID business_id) {
        List<Branch> allByBusiness_id = branchRepository.findAllByBusiness_Id(business_id);
        if (allByBusiness_id.isEmpty()) return new ApiResponse("BUSINESS NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByBusiness_id);
    }
}
