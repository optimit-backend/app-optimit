package uz.pdp.springsecurity.payload;

import lombok.Data;
import uz.pdp.springsecurity.entity.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class TaskDto {

    private UUID Id;
    private String name;
    private UUID projectId;
    private UUID taskTypeId;
    private Date startDate;
    private Date EndDate;
    private List<UUID> users;
    private UUID stage;
    private UUID taskStatus;
    private String importance;
    private UUID dependTask;
    private boolean isProduction;
    private UUID production;
    private double goalAmount;
    private double taskPrice;
    private boolean isEach;
    private UUID branchId;

}
