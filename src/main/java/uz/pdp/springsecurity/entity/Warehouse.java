package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Warehouse extends AbsEntity {
    //USE FOR SINGLE TYPE
    @ManyToOne
    private Product product;

    //USE FOR MANY TYPE
    @ManyToOne
    private ProductTypePrice productTypePrice;

    @ManyToOne
    private Branch branch;


    private double amount;
}
