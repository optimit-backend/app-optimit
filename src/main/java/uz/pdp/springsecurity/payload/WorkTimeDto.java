package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.User;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkTimeDto {
    private UUID userId;

    private Timestamp arrivalTime;

    private Timestamp leaveTime;

    private double hour;
}
