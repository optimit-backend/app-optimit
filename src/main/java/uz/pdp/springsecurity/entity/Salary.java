package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Salary extends AbsEntity {
    private double totalSumma;
    private boolean Payed;
    private Timestamp startDate;
    private Timestamp endDate;
    @ManyToOne
    private User user;
}
