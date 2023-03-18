package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyDto {

    private UUID id;
    @NotNull(message = "required line")
    private String name;
    @NotNull(message = "required line")
    private double currentCourse;
    private String description;
    private UUID businessId;
    private boolean active;
}
