package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Project extends AbsEntity {
    private String name;
    private Date startDate;
    private Date endDate;
    private Date deadline;
    private int process;
    @ManyToOne
    private ProjectType projectType;

    @ManyToOne
    private Customer customer;

    private String description;

    @ManyToMany
    private List<User> users;

    @ManyToMany
    private List<FileData> fileDataList;

    private double budget;
    @ManyToOne
    private ProjectStatus projectStatus;

    @ManyToMany
    private List<Stage> stageList;

    private double goalAmount;

    private boolean isProduction;
    private boolean expired;

    @ManyToOne
    private Branch branch;
}
