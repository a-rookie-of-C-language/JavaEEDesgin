package site.arookieofc.processor.validation;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.validation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

@Slf4j
public class ValidationProcessor {

    public static void validateMethodParameters(Method method, Object[] args) {
        if (method == null) {
            throw new IllegalArgumentException("方法不能为null");
        }
        
        Parameter[] parameters = method.getParameters();
        
        if (parameters.length != (args == null ? 0 : args.length)) {
            throw new IllegalArgumentException("参数数量不匹配");
        }
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = args[i];
            String paramName = parameter.getName();
            
            validateParameter(parameter, value, paramName);
        }
    }
    

    public static void validateParameter(Parameter parameter, Object value, String paramName) {
        // 检查@Need注解
        if (parameter.isAnnotationPresent(Need.class)) {
            Need annotation = parameter.getAnnotation(Need.class);
            validateNeed(value, paramName, annotation.message());
        }
        
        // 检查@NotNull注解
        if (parameter.isAnnotationPresent(NotNull.class)) {
            NotNull annotation = parameter.getAnnotation(NotNull.class);
            validateNotNull(value, paramName, annotation.message());
        }
        
        // 检查@NotEmpty注解
        if (parameter.isAnnotationPresent(NotEmpty.class)) {
            NotEmpty annotation = parameter.getAnnotation(NotEmpty.class);
            validateNotEmpty(value, paramName, annotation.message());
        }
        
        // 检查@Range注解
        if (parameter.isAnnotationPresent(Range.class)) {
            Range annotation = parameter.getAnnotation(Range.class);
            validateRange(value, paramName, annotation.min(), annotation.max(), annotation.message());
        }
        
        // 检查@Size注解
        if (parameter.isAnnotationPresent(Size.class)) {
            Size annotation = parameter.getAnnotation(Size.class);
            validateSize(value, paramName, annotation.min(), annotation.max(), annotation.message());
        }
        
        // 检查@Exists注解
        if (parameter.isAnnotationPresent(Exists.class)) {
            Exists annotation = parameter.getAnnotation(Exists.class);
            validateExists(value, paramName, annotation.message());
        }
    }

    public static void validateObject(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("验证对象不能为null");
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                validateField(field, value, field.getName());
            } catch (IllegalAccessException e) {
                log.error("无法访问字段: {}", field.getName(), e);
                throw new IllegalArgumentException("字段访问失败: " + field.getName());
            }
        }
    }

    public static void validateField(Field field, Object value, String fieldName) {
        // 检查@Need注解
        if (field.isAnnotationPresent(Need.class)) {
            Need annotation = field.getAnnotation(Need.class);
            validateNeed(value, fieldName, annotation.message());
        }
        
        // 检查@NotNull注解
        if (field.isAnnotationPresent(NotNull.class)) {
            NotNull annotation = field.getAnnotation(NotNull.class);
            validateNotNull(value, fieldName, annotation.message());
        }
        
        // 检查@NotEmpty注解
        if (field.isAnnotationPresent(NotEmpty.class)) {
            NotEmpty annotation = field.getAnnotation(NotEmpty.class);
            validateNotEmpty(value, fieldName, annotation.message());
        }
        
        // 检查@Range注解
        if (field.isAnnotationPresent(Range.class)) {
            Range annotation = field.getAnnotation(Range.class);
            validateRange(value, fieldName, annotation.min(), annotation.max(), annotation.message());
        }
        
        // 检查@Size注解
        if (field.isAnnotationPresent(Size.class)) {
            Size annotation = field.getAnnotation(Size.class);
            validateSize(value, fieldName, annotation.min(), annotation.max(), annotation.message());
        }
        
        // 检查@Exists注解
        if (field.isAnnotationPresent(Exists.class)) {
            Exists annotation = field.getAnnotation(Exists.class);
            validateExists(value, fieldName, annotation.message());
        }
    }

    public static void validateNeed(Object value, String fieldName, String message) {
        // 检查null
        if (value == null) {
            log.error("字段 {} {}", fieldName, message);
            throw new IllegalArgumentException(fieldName + " " + message);
        }
        
        // 检查空字符串
        if (value instanceof String strValue) {
            if (strValue.trim().isEmpty()) {
                log.error("字段 {} {}", fieldName, message);
                throw new IllegalArgumentException(fieldName + " " + message);
            }
        }
    }

    public static void validateNotNull(Object value, String fieldName, String message) {
        if (value == null) {
            log.error("字段 {} {}", fieldName, message);
            throw new IllegalArgumentException(fieldName + " " + message);
        }
    }

    public static void validateNotEmpty(Object value, String fieldName, String message) {
        if (value instanceof String strValue) {
            if (strValue.trim().isEmpty()) {
                log.error("字段 {} {}", fieldName, message);
                throw new IllegalArgumentException(fieldName + " " + message);
            }
        }
    }

    public static void validateNotEmpty(String value, String fieldName, String message) {
        if (value == null || value.trim().isEmpty()) {
            log.error("字段 {} {}", fieldName, message);
            throw new IllegalArgumentException(fieldName + " " + message);
        }
    }

    public static void validateRange(Object value, String fieldName, long min, long max, String message) {
        if (value instanceof Number numValue) {
            long longValue = numValue.longValue();
            if (longValue < min || longValue > max) {
                log.error("字段 {} 值 {} 不在范围 [{}, {}] 内", fieldName, longValue, min, max);
                throw new IllegalArgumentException(fieldName + " " + message + ": 必须在 " + min + " 和 " + max + " 之间");
            }
        }
    }

    public static void validateSize(Object value, String fieldName, int min, int max, String message) {
        if (value instanceof String strValue) {
            int length = strValue.length();
            if (length < min || length > max) {
                log.error("字段 {} 长度 {} 不在范围 [{}, {}] 内", fieldName, length, min, max);
                throw new IllegalArgumentException(fieldName + " " + message + ": 长度必须在 " + min + " 和 " + max + " 之间");
            }
        }
    }

    public static void validateExists(Object value, String fieldName, String message) {
        if (value instanceof Optional<?> optValue) {
            if (optValue.isEmpty()) {
                log.error("字段 {} {}", fieldName, message);
                throw new IllegalArgumentException(fieldName + " " + message);
            }
        }
    }
}