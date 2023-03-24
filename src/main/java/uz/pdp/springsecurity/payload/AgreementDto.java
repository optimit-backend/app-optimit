package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementDto {
    @NotNull
    private String salaryStatus;

    @NotNull
    private double price = 0;

    @NotNull
    private boolean active;
}
