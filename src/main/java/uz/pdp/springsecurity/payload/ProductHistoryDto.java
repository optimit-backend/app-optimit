package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductHistoryDto {
    private double amount;
    private double summa;
    private Date date;
    private String userFirstName;
    private String userLastName;
    private String customerSupplierName;

    public ProductHistoryDto(double amount, double summa, Date date) {
        this.amount = amount;
        this.summa = summa;
        this.date = date;
    }
}
