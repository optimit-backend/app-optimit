package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.ProjectType;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProjectTypeDto;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.ProjectTypeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectTypeServise {

    @Autowired
    ProjectTypeRepository projectTypeRepository;

    @Autowired
    BusinessRepository businessRepository;

    public ApiResponse add(ProjectTypeDto projectTypeDto) {
        Optional<Business> optionalBusiness = businessRepository.findById(projectTypeDto.getBusinessId());
        if (optionalBusiness.isEmpty()){
            return new ApiResponse("Business Not Found",false);
        }
        ProjectType projectType=new ProjectType();
        projectType.setName(projectTypeDto.getName());
        projectType.setBusiness(optionalBusiness.get());
        projectTypeRepository.save(projectType);
        return new ApiResponse("Added",true,projectType);
    }

    public ApiResponse edit(UUID id, ProjectTypeDto projectTypeDto) {
        boolean exists = projectTypeRepository.existsById(id);
        if (!exists){
            return new ApiResponse("Not Found",false);
        }
        ProjectType projectTypes = projectTypeRepository.getById(id);
        projectTypes.setName(projectTypeDto.getName());
        ProjectType projectType = projectTypeRepository.save(projectTypes);
        return new ApiResponse("Edited",true,projectType);
    }

    public ApiResponse get(UUID id) {
        Optional<ProjectType> optionalProjectType = projectTypeRepository.findById(id);
        return optionalProjectType.map(projectType -> new ApiResponse("Found", true, projectType)).orElseGet(() -> new ApiResponse("Not Found"));
    }

    public ApiResponse delete(UUID id) {
        boolean exists = projectTypeRepository.existsById(id);
        if (!exists){
            return new ApiResponse("Not Found",false);
        }
        projectTypeRepository.deleteById(id);
        return new ApiResponse("Deleted",true);
    }

    public ApiResponse getAllByBusinessId(UUID businessId) {
        List<ProjectType> projectTypeList = projectTypeRepository.findAllByBusinessId(businessId);
        if (projectTypeList.isEmpty()){
            return new ApiResponse("Not Found",false);
        }
        return new ApiResponse("Found",true,projectTypeList);
    }
}