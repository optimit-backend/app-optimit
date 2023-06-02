package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitingProductGetDto {
    private UUID productId;

    private UUID productTypePriceId;

    private String type;

    private String productName;

    private String measurement;

    private double salePrice;

    private double quantity;

    private double totalPrice;

    private double amount;
}
