package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RepaymentDebt extends AbsEntity {

    @ManyToOne
    private Customer customer;

    private Double debtSum;

    @ManyToOne
    private PaymentMethod paymentMethod;

    private Boolean delete;

    private Timestamp payDate;
}
