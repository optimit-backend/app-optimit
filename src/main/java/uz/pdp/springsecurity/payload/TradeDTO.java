package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class TradeDTO {

    private UUID customerId;

    /**
     * savdogar id
     */
    private UUID userId;

    private UUID branchId;

    private UUID paymentStatusId;

    private List<PaymentDto> paymentDtoList;

    private Date payDate;

    /**
     * umumiy summa
     */
    private double totalSum;

    /**
     * to'langan summa
     */
    private Double paidSum;

    /**
     * qarz
     */
    private double debtSum = 0d;

    /**
     * product idlari
     */
    private List<TradeProductDto> productTraderDto;
}
