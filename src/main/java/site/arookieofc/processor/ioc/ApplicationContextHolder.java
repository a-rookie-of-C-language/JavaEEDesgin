package site.arookieofc.processor.ioc;

import lombok.Setter;

public class ApplicationContextHolder {
    
    @Setter
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new RuntimeException("ApplicationContext未初始化");
        }
        return applicationContext;
    }
    
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }
    
    public static <T> T getBean(Class<T> requiredType) {
        return getApplicationContext().getBean(requiredType);
    }
    
    public static <T> T getBean(String name, Class<T> requiredType) {
        return getApplicationContext().getBean(name, requiredType);
    }
}