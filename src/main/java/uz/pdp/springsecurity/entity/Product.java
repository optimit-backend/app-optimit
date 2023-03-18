package uz.pdp.springsecurity.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;
import uz.pdp.springsecurity.enums.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product extends AbsEntity {

    @Column(nullable = false)
    private String name;

    private Date dueDate;

    private boolean active = true;

    private double tax;

    private double buyPrice;

    private double salePrice;

    private Date expireDate;

    private String barcode;

    private double minQuantity;

//    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Brand brand;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Category category;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Measurement measurement;


    @ManyToMany
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Attachment> photo;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Business business;


    @ManyToMany
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Branch> branch;
}
