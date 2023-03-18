package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.CustomerGroup;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private UUID id;

    @NotNull(message = "required line")
    private String name;

    @NotNull(message = "required line")
    private String phoneNumber;

    @NotNull(message = "enter your telegram username")
    private String telegram;

    private UUID customerGroupId;

    private UUID businessId;

    private UUID branchId;

    private double debt;
}
