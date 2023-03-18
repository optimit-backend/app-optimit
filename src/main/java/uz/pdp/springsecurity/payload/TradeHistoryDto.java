package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeHistoryDto {
    private double paidAmount;
    private Date paidDate;
    private UUID tradeId;
    private String description;
    private UUID paymentMethodId;
}
