package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class WorkTime extends AbsEntity {
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private Timestamp arrivalTime;

    private Timestamp leaveTime;

    private double hour;

    private boolean active;

    public WorkTime(User user, Timestamp arrivalTime, boolean active) {
        this.user = user;
        this.arrivalTime = arrivalTime;
        this.active = active;
    }
}
