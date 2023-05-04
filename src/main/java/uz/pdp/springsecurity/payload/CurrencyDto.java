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
    @NotNull(message = "required line")
    private double course;
    /*private UUID id;
    private String name;
    private String description;*/
}
