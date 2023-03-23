package uz.pdp.springsecurity.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.pdp.springsecurity.entity.template.AbsEntity;
import uz.pdp.springsecurity.enums.Permissions;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "fieldHandler"})
public class User extends AbsEntity implements UserDetails {


    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String email;
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    private Job job;

    private String phoneNumber;

    private boolean sex;

    private Timestamp birthday;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Role role;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Business business;

    @ManyToMany
    private Set<Branch> branches;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Attachment photo;

    private boolean active;

    private String address;

    private String description;

    private Timestamp probation;

    private String workingTime;

    private double salary;

    @ManyToMany
    private List<Bonus> bonuses;

    private Timestamp arrivalTime;

    private Timestamp timeToLeave;


    //yoqilgan
    private boolean enabled = false;
    //muddati tugamagan
    private boolean accountNonExpired = true;
    //qulflanmagan
    private boolean accountNonLocked = true;
    //Foydalanuvchining hisob ma'lumotlari (parol) muddati tugaganligini ko'rsatadi.
    private boolean credentialsNonExpired = true;


    public User(String firstName, String lastName, String username, String password, Role role, boolean enabled, Business business) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.business = business;
    }

    public User(String firstName, String lastName, String username, String password, Role role, boolean enabled, Business business, Set<Branch> branches, boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.business = business;
        this.branches = branches;
        this.isActive();
    }

    public User(String firstName, String lastName, String username, String password, Role role, boolean enabled) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    public User(String firstName, String lastName, String username, String password, Role role, Business business, Attachment photo, boolean enabled, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.business = business;
        this.photo = photo;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<Permissions> permissions = this.role.getPermissions();
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Permissions permission : permissions) {
            grantedAuthorities.add(new SimpleGrantedAuthority(permission.name()));
        }
        return grantedAuthorities;
    }

}

