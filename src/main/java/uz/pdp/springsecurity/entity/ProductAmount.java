package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ProductAmount extends AbsEntity {

//    @Column(nullable = false)
//    private Double quantity;
//
//    private double buyPrice;
//
//    private double salePrice;
//
//    private double tax;
//
//    private Date expireDate;
//
//    @ManyToOne
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private Branch branch;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;
}
