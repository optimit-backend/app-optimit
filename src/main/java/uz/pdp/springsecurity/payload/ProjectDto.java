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
public class ProjectDto {

    private String name;
    private Date startDate;
    private Date endDate;
    private Date deadline;
    private UUID projectTypeId;
    private UUID customerId;
    private String description;
    private List<UUID> userList;
    private List<UUID> attachmentList;
    private double budget;
    private UUID stageId;
    private double goalAmount;
    private boolean isProduction;
    private UUID branchId;
}
