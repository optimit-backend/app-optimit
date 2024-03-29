package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CustomerDebtRepayment extends AbsEntity {

    private Double paidSum;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private PaymentMethod paymentMethod;

    private Timestamp payDate;

}
