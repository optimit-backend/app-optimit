package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryGetDto {
    private double remain = 0;

    private double salary = 0;

    private double payedSum = 0;

    private Date startDate;

    private Date endDate;

    private boolean active;
}
