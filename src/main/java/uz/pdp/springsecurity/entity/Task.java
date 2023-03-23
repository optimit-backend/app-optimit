package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;
import uz.pdp.springsecurity.enums.Importance;

import javax.persistence.*;
import java.sql.Timestamp;
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

    private Timestamp startDate;

    private Timestamp EndDate;

    @ManyToMany
    private List<User> users;

    @ManyToOne
    private Stage stage;

    @ManyToOne
    private TaskStatus taskStatus;

    @Enumerated(EnumType.STRING)
    private Importance importance;

    @ManyToOne
    private Task dependTask;

    private boolean isProduction;

    @ManyToOne
    private Production production;

    private double goalAmount;

    private double taskPrice;

    private boolean isEach;
}


