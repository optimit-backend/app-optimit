package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    @NotNull(message = "required line")
    private String name;

    @NotNull(message = "required line")
    private Double quantity;

    @NotNull(message = "required line")
    private String barcode;

    private UUID brandId;

    private UUID categoryId;

    @NotNull(message = "required line")
    private UUID measurementId;

    private List<UUID> photoIds;

    @NotNull(message = "required line")
    private double buyPrice;

    private double salePrice;

    private double tax;

    @NotNull(message = "required line")
    private List<UUID> branchId;

    private Date expireDate;

    private Date dueDate;

    private UUID businessId;

    // types { SINGLE, MANY, COMBO }

    private String type;

    //  fields for MANY types

    private List<ProductTypePricePostDto> productTypePricePostDtoList;

    //combo
    private List<ProductTypeComboDto> productTypeComboDtoList;

}
