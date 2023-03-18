package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeProductDto {

    //USE FOR SINGLE TYPE// OR NULL
    private UUID productId;

    //USE FOR MANY TYPE// OR NULL
    private UUID productTypePriceId;

    @NotNull(message = "required line")
    private Double tradedQuantity;

    private double totalSalePrice;

//    @NotNull(message = "required line")
//    private double buyPrice;
//
}
