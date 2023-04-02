package uz.pdp.springsecurity.payload;

import lombok.Data;
import uz.pdp.springsecurity.entity.*;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class TaskDto {

    private UUID Id;
    @NotNull
    private String name;
    private UUID projectId;
    private UUID taskTypeId;
    private Date startDate;
    private Date EndDate;
    private List<UUID> users;
    private UUID taskStatus;
    private String importance;
    private UUID dependTask;
    private boolean isProduction;
    private UUID production;
    private double goalAmount;
    private double taskPrice;
    private boolean isEach;
    @NotNull
    private UUID branchId;

}
