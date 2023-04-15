package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;
import uz.pdp.springsecurity.enums.Importance;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Task extends AbsEntity {
    private String name;

    @ManyToOne
    private TaskType taskType;

    @ManyToOne
    private Project project;

    @ManyToOne
    private Stage stage;

    private Date startDate;

    private Date EndDate;

    private boolean expired;

    private Date deadLine;

    @ManyToMany
    private List<User> users;

    @ManyToOne
    private TaskStatus taskStatus;

    @Enumerated(EnumType.STRING)
    private Importance importance;

    @ManyToOne
    private Task dependTask;

    @Column(nullable = false)
    private boolean isProductions;

    @ManyToOne
    private Production production;

    @ManyToOne
    private Content content;

    private double goalAmount;

    private double taskPrice;

    private boolean isEach;
    private boolean given = false;

    @ManyToOne
    private Branch branch;
}


