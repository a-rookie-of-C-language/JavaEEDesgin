package site.arookieofc.annotation.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 懒加载注解
 * 标记的Bean将在第一次被请求时才进行初始化，而不是在容器启动时初始化
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Lazy {
    /**
     * 是否启用懒加载
     * @return true表示启用懒加载，false表示禁用懒加载
     */
    boolean value() default true;
}