package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
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

    private UUID userId;
    private String firstName;
    private String lastName;

    private Date date;

    private String description;

    private boolean given;

    private boolean task;
    private boolean lid;
    private Integer count;
    private Date deadline;
    private Integer counter;
}
