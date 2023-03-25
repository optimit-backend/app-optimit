package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.Attachment;
import uz.pdp.springsecurity.entity.Bonus;
import uz.pdp.springsecurity.entity.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {

    private String name;
    private Timestamp startDate;
    private Timestamp endDate;
    private Timestamp deadline;
    private UUID projectTypeId;
    private UUID customerId;
    private String description;
    private List<User> userList;
    private List<Attachment> attachmentList;
    private double budget;
    private UUID stageId;
    private double goalAmount;
    private boolean isProduction;
    private Bonus bonus;
    private UUID branchId;
}
