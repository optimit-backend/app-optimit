package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.SalaryStatus;
import uz.pdp.springsecurity.mapper.WorkTimeMapper;
import uz.pdp.springsecurity.payload.*;
import uz.pdp.springsecurity.repository.*;
import uz.pdp.springsecurity.util.Constants;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkTimeService {
    private final WorkTimeRepository workTimeRepository;
    private final UserRepository userRepository;
    private final WorkTimeMapper workTimeMapper;
    private final BranchRepository branchRepository;
    private final RoleRepository roleRepository;
    private final AgreementRepository agreementRepository;
    private final SalaryCountService salaryCountService;
    private final WorkTimeLateService workTimeLateService;
    private static final LocalDateTime TODAY_START = LocalDate.now().atStartOfDay();

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
        WorkTime workTime = workTimeRepository.save(
                new WorkTime(
                        branch,
                        user,
                        new Timestamp(System.currentTimeMillis()),
                        new Timestamp(System.currentTimeMillis()),
                        true
                )
        );

        countSalaryDay(workTime);
        return new ApiResponse("SUCCESS", true);
    }

    private void countSalaryDay(WorkTime workTime) {
        Optional<Agreement> optionalAgreement = agreementRepository.findByUserIdAndSalaryStatusAndActiveTrue(workTime.getUser().getId(), SalaryStatus.DAY);
        if (optionalAgreement.isPresent()){
            Agreement agreement = optionalAgreement.get();

            int count = workTimeRepository.countAllByUserIdAndBranchIdAndArrivalTimeIsBetween(workTime.getUser().getId(), workTime.getBranch().getId(), Timestamp.valueOf(TODAY_START), Timestamp.valueOf(TODAY_START.plusDays(1)));
            if (count == 1 && agreement.getPrice() > 0){
                salaryCountService.add(new SalaryCountDto(
                    1,
                    agreement.getPrice(),
                    agreement.getId(),
                    workTime.getBranch().getId(),
                    new Date(),
                    workTime.getArrivalTime() + " kuni"
                ));
            }
        }
    }

    public ApiResponse leave(WorkTimePostDto workTimePostDto) {
        if (!userRepository.existsById(workTimePostDto.getUserID())) return new ApiResponse("USER NO FOUND", false);
        Optional<WorkTime> optionalWorkTime = workTimeRepository.findByUserIdAndBranchIdAndActiveTrue(workTimePostDto.getUserID(), workTimePostDto.getBranchID());
        if (optionalWorkTime.isEmpty()) return new ApiResponse("USER DOES NOT COME", false);
        WorkTime workTime = optionalWorkTime.get();
        workTime.setLeaveTime(new Timestamp(System.currentTimeMillis()));
        long minute = (workTime.getLeaveTime().getTime() - workTime.getArrivalTime().getTime()) / (1000 * 60);
        workTime.setMinute(minute);
        workTime.setActive(false);
        workTimeLateService.add(workTime);
        // DO NOT TOUCH
        workTimeRepository.save(workTime);
        countSalaryHour(workTime);
        return new ApiResponse("SUCCESS", true);
    }

    private void countSalaryHour(WorkTime workTime) {
        Optional<Agreement> optionalAgreement = agreementRepository.findByUserIdAndSalaryStatusAndActiveTrue(workTime.getUser().getId(), SalaryStatus.HOUR);
        if (optionalAgreement.isPresent()){
            Agreement agreement = optionalAgreement.get();
            double hour = (double) (workTime.getMinute() / 6) / 10;
            if (hour > 0 && agreement.getPrice() > 0) {
                salaryCountService.add(new SalaryCountDto(
                        hour,
                        hour * agreement.getPrice(),
                        agreement.getId(),
                        workTime.getBranch().getId(),
                        new Date(),
                        workTime.getArrivalTime() + " kuni " + hour + " soat"
                ));
            }
        }
    }

    public ApiResponse getByUserLastMonth(UUID userId, UUID branchId) {
        if (!userRepository.existsById(userId)) return new ApiResponse("USER NO FOUND", false);
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NO FOUND", false);
        List<WorkTime> workTimeList = workTimeRepository.findAllByUserIdAndBranchId(userId, branchId);
        if (workTimeList.isEmpty()) return new ApiResponse("NOT FOUND WORK TIME", false);
        return new ApiResponse(true, workTimeMapper.toDtoList(workTimeList));
    }

    public ApiResponse getOnWork(UUID branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) return new ApiResponse("BRANCH NOT FOUND", false);
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

        addSalaryMonth(optionalBranch.get());
        return new ApiResponse(true, workTimeGetDtoList);
    }

    private void addSalaryMonth(Branch branch) {
        LocalDateTime todayEnd = LocalDate.now().atStartOfDay().plusDays(1);
        List<Agreement> agreementList = agreementRepository.findAllByUser_BusinessIdAndSalaryStatusAndEndDateBeforeAndActiveTrue(branch.getBusiness().getId(), SalaryStatus.MONTH, Timestamp.valueOf(todayEnd));
        for (Agreement agreement : agreementList) {
            Date endDate = agreement.getEndDate();
            LocalDateTime endDateLocal = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
            LocalDateTime startDateLocal = LocalDateTime.ofInstant(agreement.getStartDate().toInstant(), ZoneId.systemDefault());
            int days = endDateLocal.getDayOfYear() - startDateLocal.getDayOfYear();
            double salary = agreement.getPrice() * days / 30;
            if (agreement.getPrice() > 0) {
                ApiResponse apiResponse = salaryCountService.add(new SalaryCountDto(
                        1,
                        days >= 28 ? agreement.getPrice() : salary,
                        agreement.getId(),
                        branch.getId(),
                        new Date(),
                        days >= 28 ? "1 month " + new Date() : days + " kun " + new Date()
                ));
                if (apiResponse.isSuccess()) {
                    agreement.setStartDate(endDate);
                    agreement.setEndDate(Timestamp.valueOf(endDateLocal.plusMonths(1)));
                    agreementRepository.save(agreement);
                }
            }
        }
    }

    public ApiResponse getComeWork(UUID branchId) {
        LocalDateTime startMonth = LocalDate.now().atStartOfDay().withDayOfMonth(1);
        int thisDay = LocalDate.now().getDayOfMonth();
        if (!branchRepository.existsById(branchId)) return new ApiResponse("BRANCH NOT FOUND", false);
        Optional<Role> optionalRole = roleRepository.findByName(Constants.SUPERADMIN);
        if (optionalRole.isEmpty()) return new ApiResponse("ERROR", false);
        List<User> userList = userRepository.findAllByBranchesIdAndRoleIsNotAndActiveIsTrue(branchId, optionalRole.get());
        if (userList.isEmpty()) return new ApiResponse("USERS NOT FOUND", false);
        List<WorkTimeDayDto> workTimeDayDtoList = new ArrayList<>();
        double minute;
        for (User user : userList) {
            minute = 0;
            List<Timestamp> timestampList = new ArrayList<>();
            for (int day = 0; day < thisDay; day++) {
                List<WorkTime> workTimeList = workTimeRepository.findAllByUserIdAndBranchIdAndArrivalTimeIsBetween(
                        user.getId(),
                        branchId,
                        Timestamp.valueOf(startMonth.plusDays(day)),
                        Timestamp.valueOf(startMonth.plusDays(day + 1))
                );
                if (!workTimeList.isEmpty()){
                    timestampList.add(workTimeList.get(0).getArrivalTime());
                    for (WorkTime workTime : workTimeList) {
                        minute += workTime.getMinute();
                    }
                }
            }
            workTimeDayDtoList.add(new WorkTimeDayDto(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    timestampList,
                    Math.floor(minute / 6) / 10
            ));
        }
        return new ApiResponse(true, workTimeDayDtoList);
    }
}
