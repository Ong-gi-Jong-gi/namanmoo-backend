package ongjong.namanmoo.dto.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    private String title;
    private String message;
    private String token;

    @Builder
    public NotificationRequest(String title, String message, String token) {
        this.title = title;
        this.message = message;
        this.token = token;
    }
}
