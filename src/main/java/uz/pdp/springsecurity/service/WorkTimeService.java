package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Branch;
import uz.pdp.springsecurity.entity.Role;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.entity.WorkTime;
import uz.pdp.springsecurity.mapper.WorkTimeMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.WorkTimeGetDto;
import uz.pdp.springsecurity.payload.WorkTimePostDto;
import uz.pdp.springsecurity.repository.*;
import uz.pdp.springsecurity.util.Constants;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkTimeService {
    private final WorkTimeRepository workTimeRepository;
    private final UserRepository userRepository;
    private final WorkTimeMapper workTimeMapper;
    private final BranchRepository branchRepository;
    private final RoleRepository roleRepository;

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
        if (workTimeRepository.existsByUserIdAndBranchIdAndActiveTrue(user.getId(), branch.getId())) return new ApiResponse("USER ON WORK", false);
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

    public ApiResponse getByUserLastMonth(UUID userId, UUID branchId) {
        if (!userRepository.existsById(userId)) return new ApiResponse("USER NO FOUND", false);
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NO FOUND", false);
        List<WorkTime> workTimeList = workTimeRepository.findAllByUserIdAndBranchId(userId, branchId);
        if (workTimeList.isEmpty()) return new ApiResponse("NOT FOUND WORK TIME", false);
        return new ApiResponse(true, workTimeMapper.toDtoList(workTimeList));
    }

    public ApiResponse getOnWork(UUID branchId) {
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NOT FOUND", false);
        Optional<Role> optionalRole = roleRepository.findByName(Constants.SUPERADMIN);
        if (optionalRole.isEmpty()) return new ApiResponse("ERROR", false);
        List<User> userList = userRepository.findAllByBranchesIdAndRoleIsNotAndActiveIsTrue(branchId, optionalRole.get());
        if (userList.isEmpty()) return new ApiResponse("USERS NOT FOUND", false);
        List<WorkTimeGetDto> workTimeGetDtoList = new ArrayList<>();
        for (User user : userList) {
            Optional<WorkTime> optionalWorkTime = workTimeRepository.findByUserIdAndBranchIdAndActiveTrue(user.getId(), branchId);
            if (optionalWorkTime.isEmpty()){
                workTimeGetDtoList.add(new WorkTimeGetDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        new Timestamp(System.currentTimeMillis()),
                        false
                ));
            }else {
                WorkTime workTime = optionalWorkTime.get();
                workTimeGetDtoList.add(new WorkTimeGetDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        workTime.getArrivalTime(),
                        workTime.isActive()
                ));
            }
        }
        return new ApiResponse(true, workTimeGetDtoList);
    }
}
