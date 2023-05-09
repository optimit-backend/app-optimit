package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {

    private String name;

    private String message;

    private String type;

    private String notificationKay;

    private UUID shablonId;

    private UUID objectId;

    private UUID businessOrBranchId;

    private UUID userFromId;

    private UUID attachmentId;

    private List<UUID> userToId;


}
