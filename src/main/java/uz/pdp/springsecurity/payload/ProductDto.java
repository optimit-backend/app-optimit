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

    private Double minQuantity;

    private String barcode;

    private UUID brandId;

    private UUID categoryId;

    private UUID childCategoryId;

    @NotNull(message = "required line")
    private UUID measurementId;

    private UUID photoId;

    @NotNull(message = "required line")
    private double buyPrice;

    private double salePrice;


    private double profitPercent;

    private double tax;

    @NotNull(message = "required line")
    private List<UUID> branchId;

    private Date expireDate;

    private Date dueDate;

    @NotNull(message = "required line")
    private UUID businessId;

    // types { SINGLE, MANY, COMBO }

    @NotNull(message = "required line")
    private String type;

    //  fields for MANY types

    private List<ProductTypePricePostDto> productTypePricePostDtoList;

    //combo
    private List<ProductTypeComboDto> productTypeComboDtoList;

}
