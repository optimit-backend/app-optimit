package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@EqualsAndHashCode(callSuper = true)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PurchaseProduct extends AbsEntity {
    @ManyToOne
    private Purchase purchase;

    // USE FOR SINGLE TYPE
    @ManyToOne
    private Product product;

    // USE FOR MANY TYPE
    @ManyToOne
    private ProductTypePrice productTypePrice;

    private Double purchasedQuantity;

    private double buyPrice;

    private double salePrice;

    private double totalSum;
}
