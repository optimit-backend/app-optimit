package uz.pdp.springsecurity.payload;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Data
public class RepaymentDto {
    @NotNull
    private Double repayment;

    private Date payDate;

    private UUID paymentMethodId;

    private UUID branchId;
}
