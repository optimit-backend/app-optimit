package uz.pdp.springsecurity.payload;

import lombok.Data;

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
    private Date deadLine;
    private List<UUID> users;
    private UUID taskStatus;
    private String importance;
    private UUID dependTask;
    private UUID contentId;
    private boolean isProductions;
    private double goalAmount;
    private double taskPrice;
    private boolean isEach;
    @NotNull
    private UUID branchId;

}
