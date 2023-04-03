package uz.pdp.springsecurity.payload;

import lombok.Data;
import uz.pdp.springsecurity.entity.LidStatus;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class TaskDtos {

    private String name;
    private List<String> userName;
    private String taskStatusName;
    private String taskStatusOrginalName;
    private UUID userPhotoId;
    private Date date;
}
