package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;
import uz.pdp.springsecurity.enums.Lifetime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Tariff extends AbsEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    private int branchAmount;

    private long productAmount;

    private int employeeAmount;

    private long tradeAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Lifetime lifetime;

    private int testDay;

    private int interval;

    @Column(nullable = false)
    private double price;

    private double discount;

    private boolean isActive;

    private boolean isDelete;
}


