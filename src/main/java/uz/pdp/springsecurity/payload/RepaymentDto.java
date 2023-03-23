package uz.pdp.springsecurity.payload;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class RepaymentDto {
    @NotNull
    private Double repayment;

    private Date payDate;
}
