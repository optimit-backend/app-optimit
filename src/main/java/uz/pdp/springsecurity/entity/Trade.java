package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Trade extends AbsEntity {

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Customer customer;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User trader;

    @ManyToOne
    private Branch branch;

    @ManyToOne
    private PaymentStatus paymentStatus;

    /**
     *  DO NOT USE THIS FIELD/ USE PAYMENT ENTITY
     */

    @ManyToOne
    private PaymentMethod payMethod;

    private Date payDate;

    private Double totalSum;

    private Double paidSum;

    private double debtSum = 0;

    private Double totalProfit = 0.0;

    private boolean editable = true;

    @ManyToOne(cascade = CascadeType.ALL)
    private Address address;
}