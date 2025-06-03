package site.arookieofc.processor.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.Main;
import site.arookieofc.annotation.web.ControllerAdvice;
import site.arookieofc.annotation.web.ExceptionHandler;
import site.arookieofc.pojo.dto.ErrorResponse;
import site.arookieofc.processor.ioc.ApplicationContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局异常处理器
 */
@Slf4j
public class GlobalExceptionHandler {
    
    private static final Map<Class<? extends Throwable>, ExceptionHandlerInfo> exceptionHandlers = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        scanExceptionHandlers();
    }

    private static void scanExceptionHandlers() {
        try {
            // 扫描所有带有@ControllerAdvice注解的类
            String basePackage = Main.class.getPackage().getName();
            log.info("开始扫描异常处理器: {}", basePackage);
            scanPackageForExceptionHandlers(basePackage);
            log.info("异常处理器扫描完成，共注册{}个处理器", exceptionHandlers.size());
        } catch (Exception e) {
            log.error("扫描异常处理器失败", e);
        }
    }
    
    private static void scanPackageForExceptionHandlers(String packageName) {
        try {
            String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            java.net.URL resource = classLoader.getResource(packagePath);
            
            if (resource != null) {
                java.io.File directory = new java.io.File(resource.getFile());
                if (directory.exists() && directory.isDirectory()) {
                    scanDirectoryForExceptionHandlers(directory, packageName);
                }
            }
        } catch (Exception e) {
            log.error("扫描包失败: {}", packageName, e);
        }
    }
    
    private static void scanDirectoryForExceptionHandlers(java.io.File directory, String packageName) {
        java.io.File[] files = directory.listFiles();
        if (files == null) return;
        
        for (java.io.File file : files) {
            if (file.isDirectory()) {
                scanDirectoryForExceptionHandlers(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    processExceptionHandlerClass(clazz);
                } catch (ClassNotFoundException e) {
                    // 忽略无法加载的类
                }
            }
        }
    }
    
    private static void processExceptionHandlerClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(ControllerAdvice.class)) {
            log.debug("发现@ControllerAdvice类: {}", clazz.getName());
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(ExceptionHandler.class)) {
                    ExceptionHandler exceptionHandler = method.getAnnotation(ExceptionHandler.class);
                    Class<? extends Throwable>[] exceptionTypes = exceptionHandler.value();
                    
                    for (Class<? extends Throwable> exceptionType : exceptionTypes) {
                        exceptionHandlers.put(exceptionType, new ExceptionHandlerInfo(clazz, method));
                        log.debug("注册异常处理器: {} -> {}.{}", 
                                exceptionType.getName(), 
                                clazz.getSimpleName(), 
                                method.getName());
                    }
                }
            }
        }
    }

    public static boolean handleException(Throwable ex, HttpServletRequest request, HttpServletResponse response) {
        try {
            log.debug("处理异常: {}, 消息: {}", ex.getClass().getName(), ex.getMessage());
            ExceptionHandlerInfo handlerInfo = findExceptionHandler(ex.getClass());
            if (handlerInfo != null) {
                log.debug("找到异常处理器: {}.{}", 
                        handlerInfo.handlerClass.getSimpleName(), 
                        handlerInfo.method.getName());
                return invokeCustomExceptionHandler(handlerInfo, ex, request, response);
            } else {
                log.debug("未找到自定义异常处理器，使用默认处理");
                return handleDefaultException(ex, request, response);
            }
        } catch (Exception e) {
            log.error("异常处理失败", e);
            return handleDefaultException(ex, request, response);
        }
    }
    
    private static ExceptionHandlerInfo findExceptionHandler(Class<? extends Throwable> exceptionType) {
        ExceptionHandlerInfo handler = exceptionHandlers.get(exceptionType);
        if (handler != null) {
            return handler;
        }
        for (Map.Entry<Class<? extends Throwable>, ExceptionHandlerInfo> entry : exceptionHandlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(exceptionType)) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    private static boolean invokeCustomExceptionHandler(ExceptionHandlerInfo handlerInfo, Throwable ex, 
                                                       HttpServletRequest request, HttpServletResponse response) {
        try {
            Object handlerInstance = ApplicationContextHolder.getBean(handlerInfo.handlerClass);
            Method method = handlerInfo.method;
            
            // 构建方法参数
            log.debug("构建异常处理器参数: {}.{}", handlerInfo.handlerClass.getSimpleName(), method.getName());
            Object[] args = buildExceptionHandlerArguments(method, ex, request, response);
            
            // 调用异常处理方法
            log.debug("调用异常处理器方法: {}.{}", handlerInfo.handlerClass.getSimpleName(), method.getName());
            Object result = method.invoke(handlerInstance, args);
            
            // 处理返回结果
            writeResponse(response, result);
            log.debug("异常处理完成: {}", ex.getClass().getName());
            return true;
        } catch (Exception e) {
            log.error("调用自定义异常处理器失败", e);
            return false;
        }
    }
    
    private static Object[] buildExceptionHandlerArguments(Method method, Throwable ex, 
                                                          HttpServletRequest request, HttpServletResponse response) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            
            if (Throwable.class.isAssignableFrom(paramType)) {
                args[i] = ex;
            } else if (paramType == HttpServletRequest.class) {
                args[i] = request;
            } else if (paramType == HttpServletResponse.class) {
                args[i] = response;
            } else {
                args[i] = null;
            }
        }
        
        return args;
    }
    
    private static boolean handleDefaultException(Throwable ex, HttpServletRequest request, HttpServletResponse response) {
        try {
            ErrorResponse errorResponse;
            
            if (ex instanceof IllegalArgumentException) {
                log.warn("请求参数错误: {}", ex.getMessage());
                errorResponse = new ErrorResponse(400, ex.getMessage(), request.getRequestURI());
            } else if (ex instanceof NullPointerException) {
                log.error("空指针异常", ex);
                errorResponse = new ErrorResponse(500, "空指针异常", request.getRequestURI());
            } else if (ex instanceof RuntimeException) {
                log.error("运行时异常: {}", ex.getMessage(), ex);
                errorResponse = new ErrorResponse(500, "运行时异常: " + ex.getMessage(), request.getRequestURI());
            } else {
                log.error("服务器内部错误: {}", ex.getMessage(), ex);
                errorResponse = new ErrorResponse(500, "服务器内部错误: " + ex.getMessage(), request.getRequestURI());
            }
            
            writeResponse(response, errorResponse);
            return true;
        } catch (Exception e) {
            log.error("默认异常处理失败", e);
            return false;
        }
    }
    
    private static void writeResponse(HttpServletResponse response, Object result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        if (result instanceof ErrorResponse errorResponse) {
            response.setStatus(errorResponse.getCode());
            log.debug("设置错误响应状态码: {}", errorResponse.getCode());
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
        
        String jsonResponse = objectMapper.writeValueAsString(result);
        log.debug("写入错误响应: 长度={}", jsonResponse.length());
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    /**
     * 异常处理器信息
     */
    @AllArgsConstructor
    private static class ExceptionHandlerInfo {
        final Class<?> handlerClass;
        final Method method;
    }
}