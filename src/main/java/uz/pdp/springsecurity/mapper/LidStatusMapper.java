package uz.pdp.springsecurity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.pdp.springsecurity.entity.LidStatus;
import uz.pdp.springsecurity.payload.LidStatusDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LidStatusMapper {
    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "id", source = "id")
    LidStatusDto toDto(LidStatus lidStatus);

    List<LidStatusDto> toDto(List<LidStatus> lidStatusList);

    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "business.id", source = "businessId")
    LidStatus toEntity(LidStatusDto lidStatusDto);

    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "business.id", source = "businessId")
    void update(LidStatusDto lidStatusDto, @MappingTarget LidStatus lidStatus);


}
