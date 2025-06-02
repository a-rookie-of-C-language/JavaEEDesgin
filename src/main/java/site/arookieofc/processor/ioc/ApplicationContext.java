package site.arookieofc.processor.ioc;

public interface ApplicationContext extends BeanFactory {

    void refresh();

    void close();

    String[] getBeanDefinitionNames();

    String[] getBeanNamesForType(Class<?> type);
}