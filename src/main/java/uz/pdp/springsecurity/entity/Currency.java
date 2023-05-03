package uz.pdp.springsecurity.entity;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Currency extends AbsEntity {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Business business;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double course;

    private String description = "delete";

    private boolean active = true;

    public Currency(String name, Business business, double course) {
        this.name = name;
        this.business = business;
        this.course = course;
    }
}
