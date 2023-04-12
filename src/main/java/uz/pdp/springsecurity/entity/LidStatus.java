package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LidStatus extends AbsEntity {
    private String name;
    private String color;
    private Integer sort;
    private String orginalName;
    private boolean increase;
    private boolean saleStatus = false;
    @ManyToOne
    private Business business;
}