package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypePriceGetDto {

    private UUID productTypePriceId;

    private UUID productTypeValueNameId;

    private UUID photoId;

    private String productTypeName;

    private String productTypeValueName;

    @NotNull(message = "required line")
    private String barcode;

    @NotNull(message = "required line")
    private double buyPrice;

    private double salePrice;

    private double profitPercent;

    private double quantity;
}
