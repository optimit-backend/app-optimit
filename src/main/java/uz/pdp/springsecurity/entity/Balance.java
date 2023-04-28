package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;
import uz.pdp.springsecurity.enums.BalanceType;
import uz.pdp.springsecurity.enums.NotificationType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Balance extends AbsEntity {

    private double accountSumma;

    @ManyToOne
    private PaymentMethod paymentMethod;

    @ManyToOne
    private Branch branch;
}
