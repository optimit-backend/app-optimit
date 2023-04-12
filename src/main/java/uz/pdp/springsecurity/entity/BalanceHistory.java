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
public class BalanceHistory extends AbsEntity {
    private double summa;

    private boolean plus;

    private double accountSumma;

    private double totalSumma;

    @ManyToOne
    private Business business;
}
