package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.Category;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.CategoryDto;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    BusinessRepository businessRepository;

    public ApiResponse add(CategoryDto categoryDto) {
        Optional<Business> optionalBusiness = businessRepository.findById(categoryDto.getBusinessId());
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("BUSINESS NOT FOUND", false);
        }

        Category category = new Category(
                categoryDto.getName(),
                optionalBusiness.get(),
                categoryDto.getDescription()

        );

        category = categoryRepository.save(category);
        return new ApiResponse("ADDED", true, category);
    }

    public ApiResponse edit(UUID id, CategoryDto categoryDto) {
        if (!categoryRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);

        Category category = categoryRepository.getById(id);
        category.setName(categoryDto.getName());

        categoryRepository.save(category);
        return new ApiResponse("EDITED", true);
    }

    public ApiResponse get(UUID id) {
        if (!categoryRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);

        return new ApiResponse("FOUND", true, categoryRepository.findById(id).get());
    }

    public ApiResponse delete(UUID id) {
        if (!categoryRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);

        categoryRepository.deleteById(id);
        return new ApiResponse("DELETED", true);
    }


    public ApiResponse getAllByBusinessId(UUID businessId) {
//        List<Category> allByBusinessId = categoryRepository.findAllByBusiness_Id(businessId);
        List<Category> allByBusinessId = categoryRepository.findByBusinessIdAndParentCategoryNull(businessId);
        if (allByBusinessId.isEmpty()) return new ApiResponse("NOT FOUND",false);
        return new ApiResponse("FOUND",true,allByBusinessId);
    }

    public ApiResponse addChildCategory(CategoryDto categoryDto) {
        Optional<Business> optionalBusiness = businessRepository.findById(categoryDto.getBusinessId());
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("BUSINESS NOT FOUND", false);
        }
        Optional<Category> optionalCategory = categoryRepository.findById(categoryDto.getParentCategory());
        if (optionalCategory.isEmpty()) {
            return new ApiResponse("SELECTED CATEGORY NOT FOUND", false);
        }
        Category category = new Category(
                categoryDto.getName(),
                categoryDto.getDescription(),
                optionalBusiness.get(),
                optionalCategory.get()
        );
        category = categoryRepository.save(category);
        CategoryDto dto = generateCategoryDtoFromCategory(category);
        return new ApiResponse("ADDED", true, dto );
    }

    public ApiResponse getAllChildCategoryById(UUID id) {
        List<Category> allByBusiness_id = categoryRepository.findAllByBusinessIdAndParentCategoryNotNull(id);
        if (allByBusiness_id.isEmpty()){
            return new ApiResponse("CATEGORIES NOT FOUND", false);
        }
        List<CategoryDto> dtos = new ArrayList<>();
        for (Category category : allByBusiness_id) {
            dtos.add(generateCategoryDtoFromCategory(category));
        }
        return new ApiResponse("All Child Category", true,  dtos);
    }

    public CategoryDto generateCategoryDtoFromCategory(Category category){
        CategoryDto dto = new CategoryDto();
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setBusinessId(category.getBusiness().getId());
        dto.setParentCategory(category.getParentCategory().getId());
        dto.setParentCategoryName(category.getParentCategory().getName());
        return dto;
    }

    public ApiResponse getAllChildcategoryByParentId(UUID id) {
        List<Category> categoriesByParentCategoryId = categoryRepository.findCategoriesByParentCategoryId(id);
        if (categoriesByParentCategoryId.isEmpty()){
            return new ApiResponse(false,"Not yet child category");
        }
        List<CategoryDto> dtos = new ArrayList<>();
        for (Category category : categoriesByParentCategoryId) {
            dtos.add(generateCategoryDtoFromCategory(category));
        }
        return new ApiResponse("All child category",true, dtos);
    }
}
