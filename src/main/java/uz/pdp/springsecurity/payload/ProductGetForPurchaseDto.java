package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
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
    private double buyPriceDollar = 1;
    private boolean buyDollar = false;
    private double salePriceDollar = 1;
    private boolean saleDollar = false;

    private double amount;

    private double profitPercent;

    private String measurementName;

    private String brandName;

    private Date expiredDate;

    private double minQuantity;

    private UUID photoId;
}
