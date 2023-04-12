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
    private Double quantity;

    @Column(nullable = false)
    private double totalQuantity;

    @Column(nullable = false)
    private double invalid = 0d;

    @Column(nullable = false)
    private double contentPrice;

    @Column(nullable = false)
    private double cost;

    @Column(nullable = false)
    private boolean costEachOne;

    @Column(nullable = false)
    private double totalPrice;
}
