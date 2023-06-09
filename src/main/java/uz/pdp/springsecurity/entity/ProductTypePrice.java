package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class ProductTypePrice extends AbsEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    private ProductTypeValue productTypeValue; //oq

    @ManyToOne(fetch = FetchType.EAGER)
    private ProductTypeValue subProductTypeValue;//32,64,128,256

    @OneToOne(cascade = CascadeType.ALL)
    private Attachment photo;

    private String barcode;

    private double buyPrice;

    private double salePrice;
    private double buyPriceDollar = 1;
    private double salePriceDollar = 1;
    private double grossPrice;
    private double grossPriceDollar;
    private double profitPercent;
    private Boolean active;
}
