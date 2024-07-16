package ongjong.namanmoo.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final String status;
    private final String message;
    private final T data;

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}