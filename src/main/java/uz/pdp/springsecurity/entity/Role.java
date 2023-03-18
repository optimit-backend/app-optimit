package uz.pdp.springsecurity.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.pdp.springsecurity.entity.template.AbsEntity;
import uz.pdp.springsecurity.enums.Permissions;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value={"hibernateLazyInitializer","handler","fieldHandler"})
public class Role extends AbsEntity {

    @Column(nullable = false,unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @ElementCollection
    private List<Permissions> permissions;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Business business;

    private String description;


    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(updatable = false)
    @UpdateTimestamp
    private Timestamp updateAt;



    public Role(String name, List<Permissions> permissions,Business business) {
        this.name = name;
        this.permissions = permissions;
        this.business = business;
    }
    public Role(String name, List<Permissions> permissions) {
        this.name = name;
        this.permissions = permissions;
    }



}
