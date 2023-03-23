package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProfileDto;
import uz.pdp.springsecurity.payload.UserDto;
import uz.pdp.springsecurity.repository.*;
import uz.pdp.springsecurity.util.Constants;

import java.util.*;

@Service
@RequiredArgsConstructor
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

    private final SubscriptionRepository subscriptionRepository;
    private final RoleRepository roleRepository;

    public ApiResponse add(UserDto userDto, boolean isNewUser) {
        UUID businessId = userDto.getBusinessId();
        Optional<Business> optionalBusiness = businessRepository.findById(businessId);
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("NOT FOUND BUSINESS", false);
        }
        Business business = optionalBusiness.get();

        List<User> allUser = userRepository.findAllByBusiness_Id(businessId);
        int size = allUser.size();

        if (!isNewUser) {
            Optional<Subscription> optionalSubscription = subscriptionRepository.findByBusinessIdAndActiveTrue(business.getId());
            if (optionalSubscription.isEmpty()) {
                return new ApiResponse("tariff aktiv emas", false);
            }
            Subscription subscription = optionalSubscription.get();
            if (subscription.getTariff().getEmployeeAmount() >= size || subscription.getTariff().getEmployeeAmount() == 0) {

            } else {
                return new ApiResponse("You have opened a sufficient branch according to the employee", false);
            }
        }

        boolean b = userRepository.existsByUsernameIgnoreCase(userDto.getUsername());
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
        user.setActive(true);

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
        user.setBusiness(business);
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

        if (!optionalUser.get().getUsername().equals(userDto.getUsername())) {
            boolean b = userRepository.existsByUsernameIgnoreCase(userDto.getUsername());
            if (b) return new ApiResponse("USERNAME ALREADY EXISTS", false);
        }


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
        List<Branch> sortedList = new ArrayList<>(branches);
        sortedList.sort(Comparator.comparing(Branch::getCreatedAt));
        user.setBranches(branches);

        if (businessRepository.findById(userDto.getBusinessId()).isEmpty()) {
            return new ApiResponse("BUSINESS NOT FOUND", false);
        }


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
        User user = byId.get();
        if (user.getRole().getName().equals(Constants.ADMIN) || user.getRole().getName().equals(Constants.SUPERADMIN))
            return new ApiResponse("ADMINNI O'CHIRIB BO'LMAYDI", false);

        user.setActive(false);
        user.setEnabled(false);
        userRepository.save(user);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse editMyProfile(User user, ProfileDto profileDto) {
        UUID id = user.getId();
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty())
            return new ApiResponse("NOT FOUND USER");

        if (!optionalUser.get().getUsername().equals(profileDto.getUsername())) {
            boolean b = userRepository.existsByUsernameIgnoreCase(profileDto.getUsername());
            if (b) return new ApiResponse("USERNAME ALREADY EXISTS", false);
        }

        if (!profileDto.getPassword().equals(profileDto.getPrePassword()))
            return new ApiResponse("PASSWORDS ARE NOT COMPATIBLE", false);


        user.setFirstName(profileDto.getFirstName());
        user.setLastName(profileDto.getLastName());
        user.setUsername(profileDto.getUsername());

        if (profileDto.getPassword() != null && !profileDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(profileDto.getPassword()));
        }

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
        Optional<Role> optionalRole = roleRepository.findByName(Constants.SUPERADMIN);
        if (optionalRole.isEmpty()) return new ApiResponse("ERROR", false);
        Role superAdmin = optionalRole.get();
        List<User> allByBusiness_id = userRepository.findAllByBusiness_IdAndRoleIsNotAndActiveIsTrue(business_id, superAdmin);
        if (allByBusiness_id.isEmpty()) return new ApiResponse("BUSINESS NOT FOUND", false);
        return new ApiResponse("FOUND", true, allByBusiness_id);
    }


    public ApiResponse getAllByBranchId(UUID branch_id) {
        Optional<Branch> optionalBranch = branchRepository.findById(branch_id);
        if (optionalBranch.isPresent()) {
            Optional<Role> optionalRole = roleRepository.findByName(Constants.SUPERADMIN);
            if (optionalRole.isEmpty()) return new ApiResponse("ERROR", false);
            Role superAdmin = optionalRole.get();
            List<User> allByBranch_id = userRepository.findAllByBranchesIdAndRoleIsNotAndActiveIsTrue(branch_id, superAdmin);
            return new ApiResponse("FOUND", true, allByBranch_id);
        }
        return new ApiResponse("NOT FOUND", false);
    }

}
