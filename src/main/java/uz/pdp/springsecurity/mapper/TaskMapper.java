package uz.pdp.springsecurity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import uz.pdp.springsecurity.entity.Task;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.payload.TaskGetDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper()
public interface TaskMapper {

    @Mapping(target = "usersIds", source = "users")
    @Mapping(target = "taskTypeId", source = "taskType.id")
    @Mapping(target = "taskStatusId", source = "taskStatus.id")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "dependTask", source = "dependTask.id")
    @Mapping(target = "production", source = "production.id")
    TaskGetDto toDto(Task task);

    default UUID userToUserId(User user) {
        return user.getId();
    }

    default List<UUID> usersToUsersIds(List<User> users) {
        return users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    List<TaskGetDto> toDto(List<Task> taskList);
}
