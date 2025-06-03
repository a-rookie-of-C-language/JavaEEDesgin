package site.arookieofc.processor.validation;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.validation.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@SupportedAnnotationTypes({
    "site.arookieofc.annotation.validation.NotNull",
    "site.arookieofc.annotation.validation.NotEmpty",
    "site.arookieofc.annotation.validation.NotNullAndEmpty",
    "site.arookieofc.annotation.validation.Range",
    "site.arookieofc.annotation.validation.Size",
    "site.arookieofc.annotation.validation.Exists"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ValidationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log.info("ValidationProcessor 处理注解: {}", annotations);
        
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            log.info("处理注解 {} 的元素: {}", annotation.getSimpleName(), elements.size());
            
            // 在编译时处理注解
            // 这里可以生成验证代码、警告信息等
        }
        
        return true; // 表示我们已经处理了这些注解，不需要其他处理器再处理
    }
    
    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<>();
    }

    /**
     * 验证对象的所有带注解的字段
     * @param obj 要验证的对象
     * @throws IllegalArgumentException 如果验证失败
     */
    public static void validate(Object obj) {
        if (obj == null) {
            log.error("验证对象为null");
            throw new IllegalArgumentException("验证对象不能为空");
        }
        
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                
                // 检查@NotNullAndEmpty注解
                if (field.isAnnotationPresent(NotNullAndEmpty.class)) {
                    NotNullAndEmpty annotation = field.getAnnotation(NotNullAndEmpty.class);
                    String fieldName = field.getName();
                    String message = annotation.message();
                    
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
                
                // 检查@NotNull注解
                if (field.isAnnotationPresent(NotNull.class) && value == null) {
                    NotNull annotation = field.getAnnotation(NotNull.class);
                    String fieldName = field.getName();
                    String message = annotation.message();
                    log.error("字段 {} {}", fieldName, message);
                    throw new IllegalArgumentException(fieldName + " " + message);
                }
                
                // 检查@NotEmpty注解
                if (field.isAnnotationPresent(NotEmpty.class) && value instanceof String strValue) {
                    if (strValue.trim().isEmpty()) {
                        NotEmpty annotation = field.getAnnotation(NotEmpty.class);
                        String fieldName = field.getName();
                        String message = annotation.message();
                        log.error("字段 {} {}", fieldName, message);
                        throw new IllegalArgumentException(fieldName + " " + message);
                    }
                }
                
                // 检查@Range注解
                if (field.isAnnotationPresent(Range.class) && value != null) {
                    Range annotation = field.getAnnotation(Range.class);
                    String fieldName = field.getName();
                    String message = annotation.message();
                    long min = annotation.min();
                    long max = annotation.max();
                    
                    if (value instanceof Number numValue) {
                        long longValue = numValue.longValue();
                        if (longValue < min || longValue > max) {
                            log.error("字段 {} 值 {} 不在范围 [{}, {}] 内", fieldName, longValue, min, max);
                            throw new IllegalArgumentException(fieldName + " " + message + ": 必须在 " + min + " 和 " + max + " 之间");
                        }
                    }
                }
                
                // 检查@Size注解
                if (field.isAnnotationPresent(Size.class) && value instanceof String strValue) {
                    Size annotation = field.getAnnotation(Size.class);
                    String fieldName = field.getName();
                    String message = annotation.message();
                    int min = annotation.min();
                    int max = annotation.max();
                    int length = strValue.length();
                    
                    if (length < min || length > max) {
                        log.error("字段 {} 长度 {} 不在范围 [{}, {}] 内", fieldName, length, min, max);
                        throw new IllegalArgumentException(fieldName + " " + message + ": 长度必须在 " + min + " 和 " + max + " 之间");
                    }
                }
                
                // 检查@Exists注解
                if (field.isAnnotationPresent(Exists.class) && value instanceof Optional<?> optValue) {
                    if (optValue.isEmpty()) {
                        Exists annotation = field.getAnnotation(Exists.class);
                        String fieldName = field.getName();
                        String message = annotation.message();
                        log.error("字段 {} {}", fieldName, message);
                        throw new IllegalArgumentException(fieldName + " " + message);
                    }
                }
            } catch (IllegalAccessException e) {
                log.error("无法访问字段: {}", field.getName(), e);
            }
        }
    }

    /**
     * 验证字符串是否为null且不为空
     * @param value 要验证的字符串
     * @param fieldName 字段名称
     * @param message 错误消息
     * @throws IllegalArgumentException 如果字符串为null或空
     */
    public static void validateNotEmpty(String value, String fieldName, String message) {
        if (value == null || value.trim().isEmpty()) {
            log.error("字段 {} {}", fieldName, message);
            throw new IllegalArgumentException(fieldName + " " + message);
        }
    }

    /**
     * 验证Optional对象是否存在值
     * @param optional 要验证的Optional对象
     * @param fieldName 字段名称
     * @param message 错误消息
     * @param <T> Optional包含的对象类型
     * @return Optional中的值
     * @throws IllegalArgumentException 如果Optional为空
     */
    public static <T> T validateExists(Optional<T> optional, String fieldName, String message) {
        return optional.orElseThrow(() -> {
            log.error("字段 {} {}", fieldName, message);
            return new IllegalArgumentException(fieldName + " " + message);
        });
    }
}