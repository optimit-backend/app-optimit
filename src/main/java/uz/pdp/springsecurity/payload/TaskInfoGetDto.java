package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskInfoGetDto {
    private int taskAmount;
    private int doneTaskAmount;
    private int notDoneDeadlineAmount;
}
