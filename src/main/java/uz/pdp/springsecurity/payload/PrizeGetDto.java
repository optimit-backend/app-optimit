package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.*;

import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrizeGetDto {
    private UUID id;
    private UUID branchId;
    private String branchName;

    private UUID bonusId;
    private String bonusName;

    private Set<UUID> userIdSet;

    private Date date;

    private String description;

    private boolean given;

    private UUID projectId;
    private String projectName;

    private UUID taskId;
    private String taskName;

    private Date deadline;

    public PrizeGetDto(UUID id, UUID branchId, String branchName, UUID bonusId, String bonusName, Set<UUID> userIdSet, Date date, String description, boolean given, Date deadline) {
        this.id = id;
        this.bonusId = bonusId;
        this.bonusName = bonusName;
        this.userIdSet = userIdSet;
        this.date = date;
        this.description = description;
        this.given = given;
        this.deadline = deadline;
    }
}
