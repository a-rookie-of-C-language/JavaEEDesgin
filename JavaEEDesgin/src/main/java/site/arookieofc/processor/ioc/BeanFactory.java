package site.arookieofc.processor.ioc;

public interface BeanFactory {

    Object getBean(String name);

    <T> T getBean(Class<T> requiredType);

    <T> T getBean(String name, Class<T> requiredType);

    boolean containsBean(String name);

    boolean isSingleton(String name);

    Class<?> getType(String name);
}