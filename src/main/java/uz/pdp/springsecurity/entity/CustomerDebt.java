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
public class CustomerDebt extends AbsEntity {
    @ManyToOne
    private Customer customer;
    @ManyToOne
    private Trade trade;

    private Double debtSum;


    private Boolean delete;
}
