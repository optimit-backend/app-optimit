package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String username;

    private String password;

    private UUID jobId;

    private String phoneNumber;

    private boolean sex;

    private Timestamp birthday;

    private UUID roleId;

    private String roleName;

    private UUID businessId;

    private Set<UUID> branchesId;

    private UUID photoId;

    private boolean active;

    private String address;

    private String description;

    private Timestamp probation;

    private String workingTime;

    private double salary;

    private List<UUID> bonusesId;

    private Timestamp arrivalTime;

    private Timestamp leaveTime;

    private boolean enabled;
}
