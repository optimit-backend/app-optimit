package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.TaskPrice;
import uz.pdp.springsecurity.entity.User;

import java.util.Date;
import java.util.List;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskGetDto {
    private UUID id;
    private String name;

    private UUID taskTypeId;

    private String projectName;
    private UUID contentId;

    private Date startDate;

    private Date EndDate;

    private List<TaskPrice> taskPriceList;

    private UUID taskStatusId;

    private String importance;

    private UUID dependTask;

    private boolean isProductions;

    private UUID production;

    private double goalAmount;

    private double taskPrice;


    private UUID branchId;
}
