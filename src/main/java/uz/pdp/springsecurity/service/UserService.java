package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Attachment;
import uz.pdp.springsecurity.entity.Branch;
import uz.pdp.springsecurity.entity.Role;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProfileDto;
import uz.pdp.springsecurity.payload.UserDto;
import uz.pdp.springsecurity.repository.AttachmentRepository;
import uz.pdp.springsecurity.repository.BranchRepository;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.UserRepository;

import java.util.*;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleService roleService;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    AttachmentRepository attachmentRepository;

    public ApiResponse add(UserDto userDto) {
        boolean b = userRepository.existsByUsername(userDto.getUsername());
        if (b) return new ApiResponse("USER ALREADY EXISTS", false);

        ApiResponse response = roleService.get(userDto.getRoleId());
        if (!response.isSuccess())
            return response;

        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole((Role) response.getObject());


        HashSet<Branch> branches = new HashSet<>();
        for (Iterator<UUID> iterator = userDto.getBranchId().iterator(); iterator.hasNext(); ) {
            UUID branchId = iterator.next();
            Optional<Branch> optionalBranch = branchRepository.findById(branchId);
            if (optionalBranch.isPresent()) {
                branches.add(optionalBranch.get());
            } else {
                return new ApiResponse("BRANCH NOT FOUND", false);
            }
        }
        user.setBranches(branches);
//        if (optionalBranch.isEmpty()) return new ApiResponse("BRANCH NOT FOUND", false);
//        user.setBranch(optionalBranch.get());

        if (!businessRepository.existsById(userDto.getBusinessId()))
            return new ApiResponse("BUSINESS NOT FOUND", false);
        user.setBusiness(businessRepository.findById(userDto.getBusinessId()).get());

        user.setEnabled(userDto.getEnabled());

        if (userDto.getPhotoId() != null) {
            user.setPhoto(attachmentRepository.findById(userDto.getPhotoId()).orElseThrow());
        }
        userRepository.save(user);
        return new ApiResponse("ADDED", true);
    }

    public ApiResponse edit(UUID id, UserDto userDto) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) return new ApiResponse("USER NOT FOUND", false);
        boolean b = userRepository.existsByUsername(userDto.getUsername());

        if (b) return new ApiResponse("USERNAME ALREADY EXISTS", false);

        ApiResponse response = roleService.get(userDto.getRoleId());
        if (!response.isSuccess())
            return response;

        User user = optionalUser.get();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        if (userDto.getPassword() != null & userDto.getPassword().length() > 2) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

//        Optional<Branch> optionalBranch = branchRepository.findById(userDto.getBranchId());
//        if (optionalBranch.isEmpty()) return new ApiResponse("BRANCH NOT FOUND", false);
//        user.setBranch(optionalBranch.get());
        Set<Branch> branches = new HashSet<>();
        for (UUID branchId : userDto.getBranchId()) {
            Optional<Branch> byId = branchRepository.findById(branchId);
            if (byId.isPresent()) {
                branches.add(byId.get());
            } else {
                return new ApiResponse("BRANCH NOT FOUND", false);
            }
        }
        user.setBranches(branches);

        if (!businessRepository.existsById(userDto.getBusinessId()))
            return new ApiResponse("BUSINESS NOT FOUND", false);
        user.setBusiness(businessRepository.findById(userDto.getBusinessId()).get());

        user.setRole((Role) response.getObject());
        user.setEnabled(userDto.getEnabled());

        UUID photoId = userDto.getPhotoId();
        if (photoId != null) {
            Optional<Attachment> optionalPhoto = attachmentRepository.findById(photoId);
            if (optionalPhoto.isEmpty()) return new ApiResponse("PHOTO NOT FOUND", false);

            user.setPhoto(optionalPhoto.get());
        }
        userRepository.save(user);
        return new ApiResponse("EDITED", true);
    }

    public ApiResponse get(UUID id) {
        boolean exists = userRepository.existsById(id);
        if (!exists) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, userRepository.findById(id).get());
    }

    public ApiResponse delete(UUID id) {
        Optional<User> byId = userRepository.findById(id);
        if (byId.isEmpty()) return new ApiResponse("USER NOT FOUND", false);
        userRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse editMyProfile(ProfileDto profileDto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userRepository.existsByUsernameAndIdNot(profileDto.getUsername(), user.getId()))
            return new ApiResponse("USERNAME ALREADY EXISTS", false);

        if (!profileDto.getPassword().equals(profileDto.getPrePassword()))
            return new ApiResponse("PASSWORDS ARE NOT COMPATIBLE", false);


        user.setFirstName(profileDto.getFirstName());
        user.setLastName(profileDto.getLastName());
        user.setUsername(profileDto.getUsername());
        user.setPassword(passwordEncoder.encode(profileDto.getPassword()));

        Optional<Attachment> optionalPhoto = attachmentRepository.findById(profileDto.getPhotoId());
        if (optionalPhoto.isEmpty()) return new ApiResponse("PHOTO NOT FOUND", false);

        user.setPhoto(optionalPhoto.get());

        userRepository.save(user);
        return new ApiResponse("UPDATED", true);
    }

    public ApiResponse getByRole(UUID role_id) {
        List<User> allByRole_id = userRepository.findAllByRole_Id(role_id);
        if (allByRole_id.isEmpty()) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, allByRole_id);
    }

    public ApiResponse getAllByBusinessId(UUID business_id) {
        List<User> allByBusiness_id = userRepository.findAllByBusiness_Id(business_id);
        if (allByBusiness_id.isEmpty()) return new ApiResponse("BUSINESS NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByBusiness_id);
    }


    public ApiResponse getAllByBranchId(UUID branch_id) {
        Optional<Branch> optionalBranch = branchRepository.findById(branch_id);
        if (optionalBranch.isPresent()) {
            Set<Branch> branches = new HashSet<>();
            List<User> allByBranch_id = userRepository.findAllByBranchesId(branch_id);
            return new ApiResponse("FOUND", true, allByBranch_id);
        }
        return new ApiResponse("NOT FOUND", false);
    }

}
