package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Trade extends AbsEntity {

    @OneToOne
    private Customer customer;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User trader;

   /* @OneToMany
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn
    private List<TradeProduct> tradeProductList;*/

    @OneToOne
    private Branch branch;

    @ManyToOne
    private PaymentStatus paymentStatus;

    @ManyToOne
    private PaymentMethod payMethod;

    private Date payDate;

    private Double totalSum;

    private Double paidSum;

    private Double debtSum;

    private Double totalProfit = 0.0;

    @ManyToOne
    private Address address;
}