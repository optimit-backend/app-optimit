package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.Branch;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductViewDto {

    private UUID productId;
    private String productName;
    private List<Branch> branch;
    private double buyPrice;
    private double salePrice;
    private String measurementId;
    private String barcode;
    private UUID photoId;
    private double amount;
    private String brandName;
    private double minQuantity;
    private Date expiredDate;
}
