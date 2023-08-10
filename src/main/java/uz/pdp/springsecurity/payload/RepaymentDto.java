package uz.pdp.springsecurity.payload;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Data
public class RepaymentDto {
    @NotNull
    private Double repayment;

    private Timestamp payDate;

    private UUID paymentMethodId;

    private UUID branchId;
}
