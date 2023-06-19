package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerPreventedInfoDto {
    private TotalPaidSumDto totalPaidSumDto;
    private BackingProductDto backingProductDto;
    private Double debtSum;
}
