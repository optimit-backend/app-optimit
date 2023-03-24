package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Branch;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.entity.WorkTime;
import uz.pdp.springsecurity.mapper.WorkTimeMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.WorkTimePostDto;
import uz.pdp.springsecurity.repository.BranchRepository;
import uz.pdp.springsecurity.repository.UserRepository;
import uz.pdp.springsecurity.repository.WorkTimeRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkTimeService {
    private final WorkTimeRepository workTimeRepository;
    private final UserRepository userRepository;
    private final WorkTimeMapper workTimeMapper;
    private final BranchRepository branchRepository;

    public ApiResponse arrive(WorkTimePostDto workTimePostDto) {
        Optional<User> optionalUser = userRepository.findById(workTimePostDto.getUserID());
        if (optionalUser.isEmpty()) return new ApiResponse("USER NO FOUND", false);
        Branch branch = null;
        User user = optionalUser.get();
        for (Branch userBranch : user.getBranches()) {
            if (userBranch.getId().equals(workTimePostDto.getBranchID())){
                branch = userBranch;
                break;
            }
        }
        if (branch == null) return new ApiResponse("BRANCH NOT FOUND", false);
        if (workTimeRepository.existsByUserIdAndBranchIdAndActiveTrue(workTimePostDto.getUserID(), branch.getId())) return new ApiResponse("USER ON WORK", false);
        workTimeRepository.save(
                new WorkTime(
                        branch,
                        user,
                        new Timestamp(System.currentTimeMillis()),
                        new Timestamp(System.currentTimeMillis()),
                        true
                )
        );
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse leave(WorkTimePostDto workTimePostDto) {
        if (!userRepository.existsById(workTimePostDto.getUserID())) return new ApiResponse("USER NO FOUND", false);
        Optional<WorkTime> optionalWorkTime = workTimeRepository.findByUserIdAndBranchIdAndActiveTrue(workTimePostDto.getUserID(), workTimePostDto.getBranchID());
        if (optionalWorkTime.isEmpty()) return new ApiResponse("USER DOES NOT COME", false);
        WorkTime workTime = optionalWorkTime.get();
        workTime.setLeaveTime(new Timestamp(System.currentTimeMillis()));
        long hour = (workTime.getLeaveTime().getTime() - workTime.getArrivalTime().getTime()) / (1000 * 60 * 60);
        workTime.setHour(hour);
        workTime.setActive(false);
        workTimeRepository.save(workTime);
        return new ApiResponse("SUCCESS", true);
    }

    public ApiResponse getByUserLastMonth(WorkTimePostDto workTimePostDto) {
        List<WorkTime> workTimeList = workTimeRepository.findAllByUserIdAndBranchId(workTimePostDto.getUserID(), workTimePostDto.getBranchID());
        if (workTimeList.isEmpty()) return new ApiResponse("NOT FOUND WORK TIME", false);
        return new ApiResponse(true, workTimeMapper.toDtoList(workTimeList));
    }

    public ApiResponse getOnWork(UUID branchId) {
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NOT FOUND", false);
        List<WorkTime> workTimeList = workTimeRepository.findAllByBranchIdAndActiveTrue(branchId);
        if (workTimeList.isEmpty())return new ApiResponse("USER ON WORK NOT FOUND", false);
        return new ApiResponse(true, workTimeList);
    }
}
