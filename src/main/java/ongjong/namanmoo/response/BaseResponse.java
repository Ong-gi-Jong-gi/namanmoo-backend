package ongjong.namanmoo.response;

import lombok.Data;

@Data
public class BaseResponse<T> {
    private String status;
    private String message;
    private T data;

    public BaseResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
