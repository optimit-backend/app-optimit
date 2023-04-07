package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.Role;
import uz.pdp.springsecurity.exeptions.RescuersNotFoundEx;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.RoleDto;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.RoleRepository;
import uz.pdp.springsecurity.util.Constants;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;

    public ApiResponse add(RoleDto roleDto) {
        Optional<Business> optionalBusiness = businessRepository.findById(roleDto.getBusinessId());
        if (optionalBusiness.isEmpty()) return new ApiResponse("BUSINESS NOT FOUND", false);

        boolean exists = roleRepository.existsByNameIgnoreCaseAndBusinessId(roleDto.getName(), roleDto.getBusinessId());
        if (exists || roleDto.getName().equalsIgnoreCase(Constants.SUPERADMIN) || roleDto.getName().equalsIgnoreCase(Constants.ADMIN))
            return new ApiResponse("ROLE ALREADY EXISTS", false);
        Role role = new Role();
        role.setName(roleDto.getName());
        role.setPermissions(roleDto.getPermissions());
        role.setDescription(roleDto.getDescription());
        role.setBusiness(optionalBusiness.get());

        if (roleDto.getParentRole() != null) {
            Optional<Role> optionalRole = roleRepository.findById(roleDto.getParentRole());
            optionalRole.ifPresent(role::setParentRole);
        }


        roleRepository.save(role);
        return new ApiResponse("ADDED", true);
    }

    public ApiResponse edit(UUID id, RoleDto roleDto) {

        Optional<Business> optionalBusiness = businessRepository.findById(roleDto.getBusinessId());
        if (optionalBusiness.isEmpty()) return new ApiResponse("BUSINESS NOT FOUND", false);

        Optional<Role> optionalRole = roleRepository.findById(id);
        if (optionalRole.isEmpty()) return new ApiResponse("ROLE NOT FOUND", false);

        boolean exist = roleRepository.existsByNameIgnoreCaseAndBusinessIdAndIdIsNot(roleDto.getName(), roleDto.getBusinessId(), id);
        if (exist || roleDto.getName().equalsIgnoreCase(Constants.SUPERADMIN) || roleDto.getName().equalsIgnoreCase(Constants.ADMIN))
            return new ApiResponse("ROLE ALREADY EXISTS", false);

        Role role = optionalRole.get();
        role.setName(roleDto.getName());
        role.setPermissions(roleDto.getPermissions());
        role.setDescription(roleDto.getDescription());
        role.setBusiness(optionalBusiness.get());

        if (roleDto.getParentRole() != null) {
            Optional<Role> optionalParent = roleRepository.findById(roleDto.getParentRole());
            optionalParent.ifPresent(role::setParentRole);
        }

        roleRepository.save(role);
        return new ApiResponse("EDITED", true);
    }

    public ApiResponse get(@NotNull UUID id) {
        Optional<Role> optionalRole = roleRepository.findById(id);
        return optionalRole.map(role -> new ApiResponse("FOUND", true, role)).orElseThrow(() -> new RescuersNotFoundEx("Role", "id", id));
    }

    public ApiResponse delete(UUID id) {
        Optional<Role> optionalRole = roleRepository.findById(id);
        if (optionalRole.isEmpty()) return new ApiResponse("error", false);
        Role role = optionalRole.get();
        if (role.getName().equals(Constants.ADMIN)) return new ApiResponse("ERROR", false);
        roleRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getAllByBusiness(UUID business_id) {
        List<Role> allByBusiness_id = roleRepository.findAllByBusiness_IdAndNameIsNot(business_id, Constants.SUPERADMIN);

        List<RoleDto> roleDtoList = new ArrayList<>();
        for (Role role : allByBusiness_id) {
            RoleDto roleDto = new RoleDto();
            roleDto.setId(role.getId());
            roleDto.setName(role.getName());
            roleDto.setDescription(role.getDescription());
            roleDto.setPermissions(role.getPermissions());
            roleDto.setBusinessId(role.getBusiness().getId());
            if (role.getParentRole() != null) {
                roleDto.setParentRole(role.getParentRole().getId());
            }
            roleDtoList.add(roleDto);
        }

        if (roleDtoList.isEmpty()) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, roleDtoList);
    }


}
