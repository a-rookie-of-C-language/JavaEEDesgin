package site.arookieofc.processor.ioc;

public interface ApplicationContext extends BeanFactory {
    void refresh();
}