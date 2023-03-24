package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.SalaryDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalaryService {
    public ApiResponse add(SalaryDto salaryDto) {
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse edit(UUID salaryId, SalaryDto salaryDto) {
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse getAll(UUID branchId) {
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse getOne(UUID salaryId) {
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse deleteOne(UUID salaryId) {
        return new ApiResponse("SUCCESS", true);
    }
}
