package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Production extends AbsEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Branch branch;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductTypePrice productTypePrice;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private double totalQuantity;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private double invalid = 0d;

    @Column(nullable = false)
    private double totalPrice;

    @Column(nullable = false)
    private double contentPrice;

    @Column(nullable = false)
    private double cost;

    @Column(nullable = false)
    private boolean costEachOne;

    public Production(Branch branch, Date date, double totalQuantity, double quantity, double invalid, double totalPrice, double contentPrice, double cost, boolean costEachOne) {
        this.branch = branch;
        this.date = date;
        this.totalQuantity = totalQuantity;
        this.quantity = quantity;
        this.invalid = invalid;
        this.totalPrice = totalPrice;
        this.contentPrice = contentPrice;
        this.cost = cost;
        this.costEachOne = costEachOne;
    }
}
