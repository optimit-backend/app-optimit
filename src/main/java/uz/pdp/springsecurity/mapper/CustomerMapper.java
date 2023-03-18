package uz.pdp.springsecurity.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.pdp.springsecurity.entity.Customer;
import uz.pdp.springsecurity.payload.CustomerDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "customerGroupId", source = "customerGroup.id")
    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "branchId", source = "branch.id")
    CustomerDto toDto(Customer customer);

    List<CustomerDto> toDtoList(List<Customer> customers);


    @Mapping(target = "customerGroup", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerGroup.id", source = "customerGroupId")
    @Mapping(target = "business.id", source = "businessId")
    @Mapping(target = "branch.id", source = "branchId")
    Customer toEntity(CustomerDto customerDto);

    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    void update(CustomerDto customerDto, @MappingTarget Customer customer);
}
