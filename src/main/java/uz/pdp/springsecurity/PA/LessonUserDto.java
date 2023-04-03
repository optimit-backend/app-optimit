package uz.pdp.springsecurity.PA;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonUserDto {
    private UUID id;
    private UUID lessonId;
    private String lessonName;

    private UUID userId;
    private String firstName;
    private String lastName;

    private Integer view;

    private boolean finish;
}
