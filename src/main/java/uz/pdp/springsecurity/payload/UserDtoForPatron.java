package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDtoForPatron {
    private String fio;
    private List<UUID> photos;
    private String role;
    private List<ProjectDto> projectDtoList;
    private TaskInfoGetDto taskInfoGetDto;
    private List<BonusGetMetDto> bonusGetMetDtoList;
    private TradeResultDto tradeResultDto;
}
