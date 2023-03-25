package uz.pdp.springsecurity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.pdp.springsecurity.entity.SalaryCount;
import uz.pdp.springsecurity.payload.SalaryCountDto;
import uz.pdp.springsecurity.payload.SalaryCountGetDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SalaryCountMapper {
    @Mapping(target = "agreementId", source = "agreement.id")
    SalaryCountDto toDto(SalaryCount salaryCount);

    @Mapping(target = "salaryCountId", source = "id")
    @Mapping(target = "firstName", source = "agreement.user.firstName")
    @Mapping(target = "lastName", source = "agreement.user.lastName")
    @Mapping(target = "agreementName", source = "agreement.salaryStatus")
    SalaryCountGetDto toGetDto(SalaryCount salaryCount);

    List<SalaryCountGetDto> toGetDtoList(List<SalaryCount> salaryCountList);


}
