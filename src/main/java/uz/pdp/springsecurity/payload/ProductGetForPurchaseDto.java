package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.enums.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductGetForPurchaseDto {
    //USE OFR SINGLE TYPE OR NULL
    private UUID productId;

    //USE OFR SINGLE TYPE OR NULL
    private UUID productTypePriceId;

    private String type;

    private String name;

    private String barcode;

    private double buyPrice;

    private double salePrice;

    private double amount;

    private double profitPercent;

    private String measurementName;

    private String brandName;

    private Date expiredDate;

    private double minQuantity;

    private UUID photoId;
}
