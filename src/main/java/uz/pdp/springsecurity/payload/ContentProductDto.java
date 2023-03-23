package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.springsecurity.entity.Content;
import uz.pdp.springsecurity.entity.Product;
import uz.pdp.springsecurity.entity.ProductTypePrice;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentProductDto{
    private UUID productId;

    //USE FOR MANY TYPE// OR NULL

    private UUID productTypePriceId;

    @NotNull
    private double quantity;

    @NotNull
    private double totalPrice;


    private UUID ContentProductIdForEditOrNull;

    private boolean delete;

}
