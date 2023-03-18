package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProductTypePrice extends AbsEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductTypeValue productTypeValue;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Attachment photo;

    private String barcode;

    private double buyPrice;

    private double salePrice;

    private double profitPercent;
//    private double amount;
}
