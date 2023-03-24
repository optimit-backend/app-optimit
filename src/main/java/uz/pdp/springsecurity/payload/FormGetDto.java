package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.LidStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormGetDto {
    private List<LidFieldDto> lidFieldDtos;
    private SourceDto sourceDto;

}
