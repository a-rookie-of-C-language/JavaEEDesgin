package site.arookieofc.annotation.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记数值字段必须在指定范围内
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
    long min() default Long.MIN_VALUE;
    long max() default Long.MAX_VALUE;
    String message() default "值必须在指定范围内";
}