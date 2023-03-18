package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.Supplier;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.RepaymentDto;
import uz.pdp.springsecurity.payload.SupplierDto;
import uz.pdp.springsecurity.repository.BranchRepository;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.SupplierRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SupplierService {
    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    BusinessRepository businessRepository;

    public ApiResponse add(SupplierDto supplierDto) {
        Optional<Business> optionalBusiness = businessRepository.findById(supplierDto.getBusinessId());
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("BUSINESS NOT FOUND", false);
        }
        Supplier supplier = new Supplier(
                supplierDto.getName(),
                supplierDto.getPhoneNumber(),
                supplierDto.getTelegram(),
                optionalBusiness.get()
        );

        if (supplierDto.isJuridical()) {
            supplier.setJuridical(true);
            supplier.setInn(supplierDto.getInn());
            supplier.setCompanyName(supplierDto.getCompanyName());
        }
        supplier.setDebt(supplierDto.getDebt());

        supplierRepository.save(supplier);
        return new ApiResponse("ADDED", true);
    }

    public ApiResponse edit(UUID id, SupplierDto supplierDto) {
        if (!supplierRepository.existsById(id)) return new ApiResponse("supplier NOT FOUND", false);

        Supplier supplier = supplierRepository.getById(id);
        supplier.setName(supplierDto.getName());
        supplier.setPhoneNumber(supplierDto.getPhoneNumber());
        supplier.setTelegram(supplierDto.getTelegram());

        if (supplierDto.isJuridical()) {
            supplier.setJuridical(true);
            supplier.setInn(supplierDto.getInn());
            supplier.setCompanyName(supplierDto.getCompanyName());
        }


        supplier.setDebt(supplierDto.getDebt());
        supplierRepository.save(supplier);
        return new ApiResponse("EDITED", true);
    }

    public ApiResponse get(UUID id) {
        if (!supplierRepository.existsById(id)) return new ApiResponse("SUPPLIER NOT FOUND", false);
        return new ApiResponse("FOUND", true, supplierRepository.findById(id).get());
    }

    public ApiResponse delete(UUID id) {
        if (!supplierRepository.existsById(id)) return new ApiResponse("SUPPLIER NOT FOUND", false);
        supplierRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }


    public ApiResponse getAllByBusiness(UUID businessId) {
        List<Supplier> allByBusinessId = supplierRepository.findAllByBusinessId(businessId);
        if (allByBusinessId.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByBusinessId);
    }

    public ApiResponse storeRepayment(UUID id, RepaymentDto repaymentDto) {

        try {
            Optional<Supplier> supplierOptional = supplierRepository.findById(id);
            if (supplierOptional.isEmpty()) return new ApiResponse("Not Found Supplier", false);
            Supplier supplier = supplierOptional.get();

            if (repaymentDto.getRepayment() != null) {
                supplier.setDebt(supplier.getDebt() - repaymentDto.getRepayment());
                supplierRepository.save(supplier);
                return new ApiResponse("Repayment Store !", true);

            } else {
                return new ApiResponse("Brat Qarz null kelyabdi !", false);
            }

        } catch (Exception e) {
            return new ApiResponse("Exception Xatolik !", false);
        }
    }
}
