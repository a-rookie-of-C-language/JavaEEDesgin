package site.arookieofc.processor.ioc;

public interface BeanFactory {
    
    /**
     * 根据名称获取Bean
     */
    Object getBean(String name);
    
    /**
     * 根据类型获取Bean
     */
    <T> T getBean(Class<T> requiredType);
    
    /**
     * 根据名称和类型获取Bean
     */
    <T> T getBean(String name, Class<T> requiredType);
    
    /**
     * 检查是否包含指定名称的Bean
     */
    boolean containsBean(String name);
    
    /**
     * 检查Bean是否为单例
     */
    boolean isSingleton(String name);
    
    /**
     * 获取Bean的类型
     */
    Class<?> getType(String name);
}