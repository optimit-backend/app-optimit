package uz.pdp.springsecurity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.pdp.springsecurity.entity.Salary;
import uz.pdp.springsecurity.payload.SalaryGetAllDto;
import uz.pdp.springsecurity.payload.SalaryGetDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SalaryMapper {
    SalaryGetDto toDto(Salary salary);
    List<SalaryGetDto> toDtoList(List<Salary> salaryList);

    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "salaryId", source = "id")
    SalaryGetAllDto toAllDto(Salary salary);

    List<SalaryGetAllDto> toAllDtoList(List<Salary> salaryList);
}
