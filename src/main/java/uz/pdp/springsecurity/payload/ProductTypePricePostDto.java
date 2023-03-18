package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.Product;
import uz.pdp.springsecurity.entity.ProductTypeValue;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypePricePostDto{
    private UUID productTypePriceId;

    private UUID productTypeValueId;

    @NotNull(message = "required line")
    private String barcode;

    @NotNull(message = "required line")
    private double buyPrice;

    private double salePrice;

    private double profitPercent;
}
