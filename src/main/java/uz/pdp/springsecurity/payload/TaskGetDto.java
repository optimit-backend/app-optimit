package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskGetDto {
    private String name;

    private UUID taskTypeId;

    private UUID projectId;

    private Date startDate;

    private Date EndDate;

    private List<UUID> usersIds;

    private UUID taskStatusId;

    private String importance;

    private UUID dependTask;

    private boolean isProductions;

    private UUID production;

    private double goalAmount;

    private double taskPrice;

    private boolean isEach;

    private UUID branchId;
}
