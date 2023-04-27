package uz.pdp.springsecurity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uz.pdp.springsecurity.entity.Task;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.payload.TaskGetDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper()
public interface TaskMapper {

    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    @Mapping(target = "contentId", source = "content.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "userList", ignore = true)
    @Mapping(target = "taskTypeId", source = "taskType.id")
    @Mapping(target = "taskStatusId", source = "taskStatus.id")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "dependTask", source = "dependTask.id")
    @Mapping(target = "production", source = "production.id")
    @Mapping(target = "id", source = "task.id")
    TaskGetDto toDto(Task task);

    default List<User> mapUsers(Set<User> users) {
        return new ArrayList<>(users);
    }

    List<TaskGetDto> toDto(List<Task> taskList);
}
