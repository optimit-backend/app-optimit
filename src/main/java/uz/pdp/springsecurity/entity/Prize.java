package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Prize extends AbsEntity {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Branch branch;

//    @ManyToOne(optional = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private User user;

    @ManyToMany
    @JoinColumn(nullable = false)
    private Set<User> userSet;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Bonus bonus;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Task task;

    private Date deadline = new Date();

    @Column(nullable = false)
    private Date date;

    private String description;

    @Column(nullable = false)
    private boolean given;

    public Prize(Branch branch, Set<User> userSet, Bonus bonus, Date date, String description, boolean given) {
        this.branch = branch;
        this.userSet = userSet;
        this.bonus = bonus;
        this.date = date;
        this.description = description;
        this.given = given;
    }

    public Prize(Branch branch, Set<User> userSet, Bonus bonus, Project project, Date deadline, Date date, String description, boolean given) {
        this.branch = branch;
        this.userSet = userSet;
        this.bonus = bonus;
        this.project = project;
        this.deadline = deadline;
        this.date = date;
        this.description = description;
        this.given = given;
    }

    public Prize(Branch branch, Set<User> userSet, Bonus bonus, Task task, Date deadline, Date date, String description, boolean given) {
        this.branch = branch;
        this.userSet = userSet;
        this.bonus = bonus;
        this.task = task;
        this.deadline = deadline;
        this.date = date;
        this.description = description;
        this.given = given;
    }
}
