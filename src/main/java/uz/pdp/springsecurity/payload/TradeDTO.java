package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class TradeDTO {
    @NotNull
    private boolean backing;

    private UUID customerId;

    @NotNull
    private UUID userId;

    @NotNull
    private UUID branchId;

    private UUID paymentStatusId;

    @NotNull
    private List<PaymentDto> paymentDtoList;

    @NotNull
    private Date payDate;

    @NotNull
    private double totalSum;

    @NotNull
    private double paidSum;

    @NotNull
    private double debtSum;

    @NotNull
    private List<TradeProductDto> productTraderDto;

    private boolean lid;

    private String dollar;

    private String gross;
}
