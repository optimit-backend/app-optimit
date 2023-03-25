package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Project extends AbsEntity {
    private String name;
    private Timestamp startDate;
    private Timestamp endDate;
    private Timestamp deadline;
    @ManyToOne
    private ProjectType projectType;

    @ManyToOne
    private Customer customer;

    private String description;

    @ManyToMany
    private List<User> users;

    @OneToMany
    private List<Attachment> attachmentList;

    private double budget;

    @ManyToOne
    private Stage stage;

    private double goalAmount;

    private boolean isProduction;

    @ManyToOne
    private Bonus bonus;

    //todo aniqlik kiritish kerak
//    private List<ProductionAndAmount> amountList;

    @ManyToOne
    private Branch branch;
}
