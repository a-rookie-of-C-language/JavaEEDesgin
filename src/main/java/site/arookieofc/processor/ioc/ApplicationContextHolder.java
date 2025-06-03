package site.arookieofc.processor.ioc;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationContextHolder {
    
    @Setter
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            log.error("尝试访问未初始化的ApplicationContext");
            throw new RuntimeException("ApplicationContext未初始化");
        }
        log.trace("获取ApplicationContext实例");
        return applicationContext;
    }
    
    public static Object getBean(String name){
        log.debug("通过名称获取Bean: {}", name);
        return getApplicationContext().getBean(name);
    }
    
    public static <T> T getBean(Class<T> requiredType) {
        log.debug("通过类型获取Bean: {}", requiredType.getName());
        return getApplicationContext().getBean(requiredType);
    }
    
    public static <T> T getBean(String name, Class<T> requiredType) {
        log.debug("通过名称和类型获取Bean: {}, {}", name, requiredType.getName());
        return getApplicationContext().getBean(name, requiredType);
    }
    
    public static void setApplicationContext(ApplicationContext context) {
        log.info("设置ApplicationContext: {}", context != null ? context.getClass().getName() : "null");
        applicationContext = context;
    }
}