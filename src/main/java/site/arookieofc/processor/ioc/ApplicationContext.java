package site.arookieofc.processor.ioc;

public interface ApplicationContext extends BeanFactory {
    
    /**
     * 刷新容器
     */
    void refresh();
    
    /**
     * 关闭容器
     */
    void close();
    
    /**
     * 获取所有Bean名称
     */
    String[] getBeanDefinitionNames();
    
    /**
     * 根据类型获取所有Bean名称
     */
    String[] getBeanNamesForType(Class<?> type);
}