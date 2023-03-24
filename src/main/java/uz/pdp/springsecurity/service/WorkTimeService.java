package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.entity.WorkTime;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.repository.UserRepository;
import uz.pdp.springsecurity.repository.WorkTimeRepository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkTimeService {
    private final WorkTimeRepository workTimeRepository;
    private final UserRepository userRepository;

    public ApiResponse arrive(UUID userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) return new ApiResponse("USER NO FOUND", false);
        if (workTimeRepository.existsByUserIdAndActiveTrue(userId)) return new ApiResponse("USER ON WORK", false);
        workTimeRepository.save(
                new WorkTime(
                        optionalUser.get(),
                        new Timestamp(System.currentTimeMillis()),
                        true
                )
        );
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse leave(UUID userId) {
        if (!userRepository.existsById(userId)) return new ApiResponse("USER NO FOUND", false);
        if (!workTimeRepository.existsByUserIdAndActiveTrue(userId)) return new ApiResponse("USER DOES NOT COME", false);

        return null;
    }

    public ApiResponse getAll(UUID userId) {
        return null;
    }

    public ApiResponse getOnWork(UUID branchId) {
        return null;
    }
}
