package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProductTypePrice extends AbsEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    private ProductTypeValue productTypeValue;

    @OneToOne(cascade = CascadeType.ALL)
    private Attachment photo;

    private String barcode;

    private double buyPrice;

    private double salePrice;
    private double buyPriceDollar = 1;
    private double salePriceDollar = 1;

    private double profitPercent;
}
