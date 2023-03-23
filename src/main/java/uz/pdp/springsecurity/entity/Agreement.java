package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;
import uz.pdp.springsecurity.enums.SalaryStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Agreement extends AbsEntity {
    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private SalaryStatus salaryStatus;

    private double price;
}
