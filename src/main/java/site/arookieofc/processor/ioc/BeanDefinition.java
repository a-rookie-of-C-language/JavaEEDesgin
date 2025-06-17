package site.arookieofc.processor.ioc;

import lombok.Getter;
import lombok.Setter;
import java.lang.reflect.Method;

@Setter
@Getter
public class BeanDefinition {
    private String beanName;
    private Class<?> beanClass;
    private Object instance;
    private boolean singleton = true;
    private boolean lazy = false;  // 添加懒加载标识
    private Method factoryMethod; // 用于@Bean注解的方法
    private Object factoryBean;   // 包含@Bean方法的对象
    
    public BeanDefinition(String beanName, Class<?> beanClass) {
        this.beanName = beanName;
        this.beanClass = beanClass;
    }
    
    public BeanDefinition(String beanName, Class<?> beanClass, Method factoryMethod, Object factoryBean) {
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.factoryMethod = factoryMethod;
        this.factoryBean = factoryBean;
    }
}