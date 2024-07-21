package ongjong.namanmoo.dto.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotificationRequest {
    private String title;
    private String message;
    private String token;

}
