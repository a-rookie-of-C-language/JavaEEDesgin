package site.arookieofc.annotation.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SQL {
    /**
     * SQL语句
     */
    String value();
    
    /**
     * 操作类型：SELECT, INSERT, UPDATE, DELETE
     */
    String type() default "SELECT";
}