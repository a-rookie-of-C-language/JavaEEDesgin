package site.arookieofc.annotation.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字符串字段长度必须在指定范围内
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Size {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
    String message() default "长度必须在指定范围内";
}