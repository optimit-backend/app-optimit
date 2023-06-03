package uz.pdp.springsecurity.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.pdp.springsecurity.entity.Car;
import uz.pdp.springsecurity.payload.CarDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarMapper {
    @Mapping(source = "businessId", target = "business.id")
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    Car toEntity(CarDto carDto);

    @Mapping(source = "business.id", target = "businessId")
    CarDto toDto(Car car);

    List<CarDto> toDto(List<Car> cars);

    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    void update(CarDto carDto, @MappingTarget Car car);
}
