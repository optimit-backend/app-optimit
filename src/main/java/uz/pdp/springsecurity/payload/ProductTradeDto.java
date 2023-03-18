package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTradeDto {

    @NotNull(message = "required line")
    private Double tradedQuantity;

//    @NotNull(message = "required line")
//    private double buyPrice;
//
//    @NotNull(message = "required line")
//    private double salePrice;

    @NotNull(message = "required line")
    private UUID productTradeId;

}
