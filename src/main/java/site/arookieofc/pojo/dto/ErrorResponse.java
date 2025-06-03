package site.arookieofc.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一异常响应实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int code;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private Object data;

    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public ErrorResponse(int code, String message, String path) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

}