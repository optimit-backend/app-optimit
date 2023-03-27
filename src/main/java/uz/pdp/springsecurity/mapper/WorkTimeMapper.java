package uz.pdp.springsecurity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.pdp.springsecurity.entity.WorkTime;
import uz.pdp.springsecurity.payload.WorkTimeDto;
import uz.pdp.springsecurity.payload.WorkTimeGetDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkTimeMapper {
    WorkTimeDto toDto(WorkTime workTime);

    List<WorkTimeDto> toDtoList(List<WorkTime> workTimeList);

//    @Mapping(target = "firstName", source = "user.firstName")
//    @Mapping(target = "lastName", source = "user.lastName")
//    @Mapping(target = "userId", source = "user.id")
//    WorkTimeGetDto toGetDto(WorkTime workTime);
//
//    List<WorkTimeGetDto> toGetDtoList(List<WorkTime> workTimeList);
}
