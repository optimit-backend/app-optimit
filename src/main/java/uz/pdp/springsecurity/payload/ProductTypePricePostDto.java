package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypePricePostDto{

    private UUID productTypePriceId;

    private UUID productTypeValueId;

    private UUID photoId;

    private String barcode;

    @NotNull(message = "required line")
    private double buyPrice;
    private double salePrice;
    private double grossPrice;
    private double profitPercent;
}
