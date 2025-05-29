package site.arookieofc.pojo.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter  // 添加Lombok的@Getter注解
public class Result {
    private int code;
    private String msg;
    private Object data;

    public static Result success(String msg, Object data) {
        return new Result(200, msg, data);
    }

    public static Result success(String msg) {
        return new Result(200, msg, null);
    }
    public static Result success(Object data) {
        return new Result(200, "success", data);
    }
    public static Result success() {
        return new Result(200, "success", null);
    }
    public static Result error(String msg, Object data) {
        return new Result(500, msg, data);
    }
    public static Result error(String msg) {
        return new Result(500, msg, null);
    }
    public static Result error(Object data) {
        return new Result(500, "error", data);
    }
    public static Result error() {
        return new Result(500, "error", null);
    }
    public static Result error(int code, String msg) {
        return new Result(code, msg, null);
    }
    public static Result error(int code, String msg, Object data) {
        return new Result(code, msg, data);
    }

}
