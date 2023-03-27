package uz.pdp.springsecurity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.pdp.springsecurity.entity.Lid;
import uz.pdp.springsecurity.payload.LidDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LidMapper {
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Lid toEntity(LidDto lidDto);

    LidDto toDto(Lid lid);

    List<LidDto> toDto(List<Lid> lidList);
}
