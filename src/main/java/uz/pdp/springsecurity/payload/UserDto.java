package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @NotNull(message = "required line")
    private String firstName;
    @NotNull(message = "required line")
    private String lastName;
    @NotNull(message = "required line")
    private String username;

    private String password;
    @NotNull(message = "required line")
    private UUID roleId;
    @NotNull(message = "required line")
    private List<UUID> branchId;
    @NotNull(message = "required line")
    private UUID businessId;
    @NotNull(message = "required line")
    private Boolean enabled;

    private UUID photoId;
}
