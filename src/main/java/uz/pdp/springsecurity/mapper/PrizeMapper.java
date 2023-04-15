package uz.pdp.springsecurity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.pdp.springsecurity.entity.Prize;
import uz.pdp.springsecurity.payload.PrizeGetDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrizeMapper {
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "bonusId", source = "bonus.id")
    @Mapping(target = "bonusName", source = "bonus.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    PrizeGetDto toDto(Prize prize);

    List<PrizeGetDto> toDtoList(List<Prize> prizeList);
}
