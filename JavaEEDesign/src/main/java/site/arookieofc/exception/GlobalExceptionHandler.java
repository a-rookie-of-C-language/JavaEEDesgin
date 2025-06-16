package site.arookieofc.exception;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.web.ControllerException;
import site.arookieofc.annotation.web.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import site.arookieofc.pojo.dto.Result;

@Slf4j
@Component
@ControllerException
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    public Result handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("参数异常: {}", ex.getMessage(),ex);
        return Result.error(400, "参数错误: " + ex.getMessage());
    }

    @ExceptionHandler({NullPointerException.class})
    public Result handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        log.error("空指针异常", ex);
        return Result.error(500, "系统内部错误", request.getRequestURI());
    }

    @ExceptionHandler({RuntimeException.class})
    public Result handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("运行时异常: {}", ex.getMessage(), ex);
        return Result.error(500, "服务器内部错误: " + ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({Exception.class})
    public Result handleException(Exception ex, HttpServletRequest request) {
        log.error("未知异常: {}", ex.getMessage(), ex);
        return Result.error(500, "系统异常，请联系管理员", request.getRequestURI());
    }
}