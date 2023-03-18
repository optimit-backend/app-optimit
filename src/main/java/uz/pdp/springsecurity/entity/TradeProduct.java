package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TradeProduct extends AbsEntity {
    @ManyToOne
    private Trade trade;

    //USE FOR SINGLE TYPE// OR NULL
    @ManyToOne
    private Product product;

    //USE FOR MANY TYPE// OR NULL
    @ManyToOne
    private ProductTypePrice productTypePrice;

    private Double tradedQuantity;

//    private double buyPrice;
//
    private double totalSalePrice;

    //TOTAL PROFIT OF PRODUCT
    private double profit = 0;
}
