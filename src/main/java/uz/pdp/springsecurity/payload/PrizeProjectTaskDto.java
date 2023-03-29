package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrizeProjectTaskDto {
    @NotNull(message = "REQUIRED")
    private UUID branchId;

    @NotNull(message = "REQUIRED")
    private UUID bonusId;

    @NotNull(message = "REQUIRED")
    private UUID projectOrTaskId;

    @NotNull(message = "REQUIRED")
    private Set<UUID> userIdSet;

    @NotNull(message = "REQUIRED")
    private Date date;

    @NotNull(message = "REQUIRED")
    private boolean given;

    @NotNull(message = "REQUIRED")
    private Date deadline;

    private String description;
}
