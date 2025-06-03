package site.arookieofc.exception;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.web.ControllerException;
import site.arookieofc.annotation.web.ExceptionHandler;
import site.arookieofc.pojo.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
@ControllerException
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("参数异常: {}", ex.getMessage());
        return new ErrorResponse(400, "参数错误: " + ex.getMessage());
    }

    @ExceptionHandler({NullPointerException.class})
    public ErrorResponse handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        log.error("空指针异常", ex);
        return new ErrorResponse(500, "系统内部错误", request.getRequestURI());
    }

    @ExceptionHandler({RuntimeException.class})
    public ErrorResponse handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("运行时异常: {}", ex.getMessage(), ex);
        return new ErrorResponse(500, "服务器内部错误: " + ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({Exception.class})
    public ErrorResponse handleException(Exception ex, HttpServletRequest request) {
        log.error("未知异常: {}", ex.getMessage(), ex);
        return new ErrorResponse(500, "系统异常，请联系管理员", request.getRequestURI());
    }
}