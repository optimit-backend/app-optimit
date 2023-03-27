package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.LidField;
import uz.pdp.springsecurity.entity.LidStatus;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LidDto {
    private Map<LidField, String> values;

    private LidStatus lidStatus;
}
