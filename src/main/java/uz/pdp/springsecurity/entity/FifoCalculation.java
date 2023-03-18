package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FifoCalculation extends AbsEntity {
    @ManyToOne
    private Product product;

    @ManyToOne
    private ProductTypePrice productTypePrice;

    @ManyToOne
    private Branch branch;

    private double purchasedAmount;

    private double remainAmount;

    private double buyPrice;

    private boolean active = true;

    private Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    private Purchase purchase;

    public FifoCalculation(Branch branch, double purchasedAmount, double remainAmount, double buyPrice, Date date, Purchase purchase) {
        this.branch = branch;
        this.purchasedAmount = purchasedAmount;
        this.remainAmount = remainAmount;
        this.buyPrice = buyPrice;
        this.date = date;
        this.purchase = purchase;
    }


}
