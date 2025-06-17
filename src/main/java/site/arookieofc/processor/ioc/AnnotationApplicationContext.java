package site.arookieofc.processor.ioc;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.Main;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Bean;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.ioc.Lazy;
import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.processor.sql.SQLExecutor;
import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.lang.reflect.Parameter;

@Slf4j
public class AnnotationApplicationContext implements ApplicationContext {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    // 一级缓存：完成品单例对象
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    // 二级缓存：早期单例对象（已实例化但未完成依赖注入）
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    // 三级缓存：单例工厂（用于创建早期对象）
    private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>();
    // 正在创建的单例Bean名称集合
    private final Set<String> singletonsCurrentlyInCreation = Collections.synchronizedSet(new HashSet<>());

    private final String[] basePackages;

    public AnnotationApplicationContext() {
        log.info("创建AnnotationApplicationContext，使用默认包: {}", Main.class.getPackage().getName());
        this.basePackages = new String[]{Main.class.getPackage().getName()};
        refresh();
    }

    public AnnotationApplicationContext(String... basePackages) {
        log.info("创建AnnotationApplicationContext，指定包: {}", String.join(", ", basePackages));
        this.basePackages = basePackages;
        refresh();
    }

    @Override
    public void refresh() {
        log.info("刷新ApplicationContext...");
        scanComponents();
        scanDAOInterfaces();
        preInstantiateSingletons();
        log.info("ApplicationContext刷新完成，共加载 {} 个Bean定义", beanDefinitionMap.size());
    }

    private void scanComponents() {
        log.debug("开始扫描组件...");
        for (String basePackage : basePackages) {
            log.debug("扫描包: {}", basePackage);
            scanPackage(basePackage);
        }
    }

    private void scanDAOInterfaces() {
        log.debug("开始扫描DAO接口...");
        for (String basePackage : basePackages) {
            log.debug("扫描DAO包: {}", basePackage);
            scanDAOPackage(basePackage);
        }
    }

    private void scanDAOPackage(String packageName) {
        try {
            String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(packagePath);

            if (resource != null) {
                File directory = new File(resource.getFile());
                if (directory.exists() && directory.isDirectory()) {
                    scanDAODirectory(directory, packageName);
                }
            }
        } catch (Exception e) {
            log.error("扫描DAO包失败: {}, {}", packageName, e.getMessage(),e);
        }
    }

    private void scanDAODirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDAODirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    processDAOInterface(clazz);
                } catch (ClassNotFoundException e) {
                    // 忽略无法加载的类
                }
            }
        }
    }

    private void processDAOInterface(Class<?> clazz) {
        // 检查是否是DAO接口（接口且包含@SQL注解的方法）
        if (clazz.isInterface() && isDAOInterface(clazz)) {
            String beanName = clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);
            Object daoProxy = createDAOProxy(clazz);
            singletonObjects.put(beanName, daoProxy);
            BeanDefinition beanDefinition = new BeanDefinition(beanName, clazz);
            beanDefinition.setInstance(daoProxy);
            beanDefinitionMap.put(beanName, beanDefinition);
        }
    }

    private boolean isDAOInterface(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(SQL.class)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T> T createDAOProxy(Class<T> daoInterface) {
        return (T) Proxy.newProxyInstance(
            daoInterface.getClassLoader(),
            new Class<?>[]{daoInterface},
            (proxy, method, args) -> {
                try {
                    return SQLExecutor.executeSQL(method, args, method.getReturnType());
                } catch (Exception e) {
                    log.error("执行DAO方法失败: {}, {}", method.getDeclaringClass().getName(), e.getMessage(),e);
                    throw new RuntimeException("执行DAO方法失败: " + method.getName(), e);
                }
            }
        );
    }

    private void scanPackage(String packageName) {
        try {
            String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(packagePath);

            if (resource != null) {
                File directory = new File(resource.getFile());
                if (directory.exists() && directory.isDirectory()) {
                    scanDirectory(directory, packageName);
                }
            }
        } catch (Exception e) {
            log.error("扫描包失败: {}, {}", packageName, e.getMessage(),e);
        }
    }

    private void scanDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    processClass(clazz);
                } catch (ClassNotFoundException e) {
                    // 忽略无法加载的类
                }
            }
        }
    }

    private void processClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getAnnotation(Component.class);
            String beanName = component.value().isEmpty() ? 
                clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1) : 
                component.value();
            
            BeanDefinition beanDefinition = new BeanDefinition(beanName, clazz);
            
            // 检查是否有@Lazy注解
            if (clazz.isAnnotationPresent(Lazy.class)) {
                Lazy lazy = clazz.getAnnotation(Lazy.class);
                beanDefinition.setLazy(lazy.value());
                log.debug("Bean {} 标记为懒加载: {}", beanName, lazy.value());
            }
            
            beanDefinitionMap.put(beanName, beanDefinition);
            log.debug("注册Bean定义: {} -> {}", beanName, clazz.getName());
            
            // 处理@Bean方法
            processBeanMethods(clazz, beanName);
        }
    }

    private void processBeanMethods(Class<?> clazz, String factoryBeanName) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Bean.class)) {
                Bean bean = method.getAnnotation(Bean.class);
                String beanName = bean.value().isEmpty() ? method.getName() : bean.value();
                
                BeanDefinition beanDefinition = new BeanDefinition(beanName, method.getReturnType(), method, null);
                beanDefinition.setFactoryBean(factoryBeanName); // 存储工厂Bean名称而不是实例
                
                // 检查方法上的@Lazy注解
                if (method.isAnnotationPresent(Lazy.class)) {
                    Lazy lazy = method.getAnnotation(Lazy.class);
                    beanDefinition.setLazy(lazy.value());
                    log.debug("Bean方法 {} 标记为懒加载: {}", beanName, lazy.value());
                }
                
                beanDefinitionMap.put(beanName, beanDefinition);
                log.debug("注册Bean方法定义: {} -> {}.{}", beanName, clazz.getName(), method.getName());
            }
        }
    }

    private void preInstantiateSingletons() {
        log.debug("开始预实例化单例Bean...");
        List<String> beanNames = new ArrayList<>(beanDefinitionMap.keySet());
        
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            // 只有非懒加载的单例Bean才会在启动时初始化
            if (beanDefinition.isSingleton() && !beanDefinition.isLazy()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    log.error("预实例化Bean失败: {}, 错误: {}", beanName, e.getMessage(), e);
                }
            } else if (beanDefinition.isLazy()) {
                log.debug("跳过懒加载Bean的预实例化: {}", beanName);
            }
        }
        log.debug("预实例化完成");
    }

    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new RuntimeException("Bean not found: " + beanName);
        }

        // 对于懒加载的Bean，在第一次获取时进行日志记录
        if (beanDefinition.isLazy() && !singletonObjects.containsKey(beanName)) {
            log.info("首次获取懒加载Bean: {}", beanName);
        }

        if (beanDefinition.isSingleton()) {
            return getSingleton(beanName, () -> createBean(beanName, beanDefinition));
        } else {
            return createBean(beanName, beanDefinition);
        }
    }

    /**
     * 从三级缓存中获取单例对象
     */
    private Object getSingleton(String beanName) {
        // 一级缓存
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (singletonObjects) {
                // 二级缓存
                singletonObject = earlySingletonObjects.get(beanName);
                if (singletonObject == null) {
                    // 三级缓存
                    ObjectFactory<?> singletonFactory = singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        singletonObject = singletonFactory.getObject();
                        // 从三级缓存移到二级缓存
                        earlySingletonObjects.put(beanName, singletonObject);
                        singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    /**
     * 获取单例对象，支持循环依赖
     */
    private Object getSingleton(String beanName, Supplier<Object> singletonFactory) {
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                // 检查二级缓存
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject == null) {
                    // 检查三级缓存
                    ObjectFactory<?> singletonFactory3 = this.singletonFactories.get(beanName);
                    if (singletonFactory3 != null) {
                        singletonObject = singletonFactory3.getObject();
                        // 从三级缓存移到二级缓存
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    } else {
                        // 开始创建Bean
                        beforeSingletonCreation(beanName);
                        try {
                            singletonObject = singletonFactory.get();
                        } catch (Exception ex) {
                            log.error("创建单例Bean失败: {}", beanName, ex);
                            throw ex;
                        } finally {
                            afterSingletonCreation(beanName);
                        }
                        // 将完成的Bean放入一级缓存
                        addSingleton(beanName, singletonObject);
                    }
                }
            }
            return singletonObject;
        }
    }

    /**
     * 单例创建前的准备工作
     */
    private void beforeSingletonCreation(String beanName) {
        if (!this.singletonsCurrentlyInCreation.add(beanName)) {
            // 检测到循环依赖时，记录警告但允许继续创建
            log.warn("检测到循环依赖: {}, 当前创建中的Bean: {}", beanName, this.singletonsCurrentlyInCreation);
        }
    }

    /**
     * 单例创建后的清理工作
     */
    private void afterSingletonCreation(String beanName) {
        if (!this.singletonsCurrentlyInCreation.remove(beanName)) {
            log.warn("单例Bean创建完成，但未在创建列表中找到: {}", beanName);
        }
    }

    /**
     * 检查Bean是否正在创建中
     */
    private boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    /**
     * 将完成的单例Bean添加到一级缓存
     */
    private void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
        }
    }

    /**
     * 添加单例工厂到三级缓存
     */
    private void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
            }
        }
    }

    /**
     * 创建Bean实例
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        try {
            Object bean;
            
            // 创建Bean实例
            if (beanDefinition.getFactoryMethod() != null) {
                bean = createBeanByFactoryMethod(beanDefinition);
            } else {
                bean = beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
            }
            
            // 立即将早期Bean引用放入三级缓存（解决循环依赖的关键）
            if (beanDefinition.isSingleton()) {
                Object finalBean = bean;
                addSingletonFactory(beanName, () -> finalBean);
            }
            
            // 属性填充
            populateBean(bean, beanDefinition);
            
            // 应用验证拦截器（新增）
            if (needsValidationProxy(bean)) {
                bean = site.arookieofc.processor.validation.ValidationInterceptor.createProxy(bean);
            }
            
            return bean;
        } catch (Exception e) {
            log.error("创建Bean失败: {}", beanName, e);
            throw new RuntimeException("创建Bean失败: " + beanName, e);
        }
    }
    
    // 新增方法：检查是否需要验证代理
    private boolean needsValidationProxy(Object bean) {
        Class<?> clazz = bean.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                if (hasValidationAnnotation(parameter)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // 新增方法：检查参数是否有验证注解
    private boolean hasValidationAnnotation(Parameter parameter) {
        return parameter.isAnnotationPresent(site.arookieofc.annotation.validation.Need.class) ||
               parameter.isAnnotationPresent(site.arookieofc.annotation.validation.NotNull.class) ||
               parameter.isAnnotationPresent(site.arookieofc.annotation.validation.NotEmpty.class) ||
               parameter.isAnnotationPresent(site.arookieofc.annotation.validation.Range.class) ||
               parameter.isAnnotationPresent(site.arookieofc.annotation.validation.Size.class) ||
               parameter.isAnnotationPresent(site.arookieofc.annotation.validation.Exists.class);
    }

    /**
     * 填充Bean的属性（依赖注入）
     */
    private void populateBean(Object bean, BeanDefinition beanDefinition) {
        Class<?> clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                try {
                    Object dependency = getBeanByType(field.getType());
                    if (dependency != null) {
                        field.set(bean, dependency);
                    } else {
                        log.warn("无法找到类型为 {} 的依赖", field.getType().getName());
                    }
                } catch (Exception e) {
                    log.error("依赖注入失败: {}", field.getName(), e);
                    throw new RuntimeException("依赖注入失败: " + field.getName(), e);
                }
            }
        }
    }

    private Object createBeanByFactoryMethod(BeanDefinition beanDefinition) {
        Method factoryMethod = beanDefinition.getFactoryMethod();
        Object factoryBean = beanDefinition.getFactoryBean();

        // 如果factoryBean是String类型（Bean名称），需要获取实际的Bean实例
        if (factoryBean instanceof String factoryBeanName) {
            factoryBean = getBean(factoryBeanName);
            beanDefinition.setFactoryBean(factoryBean); // 更新为实际实例
        } else if (factoryBean == null) {
            String factoryBeanName = null;
            for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
                if (entry.getValue().getBeanClass() == factoryMethod.getDeclaringClass() &&
                        entry.getValue().getFactoryMethod() == null) {
                    factoryBeanName = entry.getKey();
                    break;
                }
            }
    
            if (factoryBeanName != null) {
                factoryBean = getBean(factoryBeanName);
                beanDefinition.setFactoryBean(factoryBean);
            }
        }
    
        factoryMethod.setAccessible(true);
        try {
            return factoryMethod.invoke(factoryBean);
        } catch (NoSuchMethodError | IllegalAccessException | InvocationTargetException e) {
            log.error("createBeanByFactoryMethod error {}", beanDefinition.getBeanClass().getName(), e);
            throw new RuntimeException("createBeanByFactoryMethod error: " + beanDefinition.getBeanClass().getName(), e);
        }
    }

    private Object getBeanByType(Class<?> type) {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            BeanDefinition beanDefinition = entry.getValue();
            if (type.isAssignableFrom(beanDefinition.getBeanClass())) {
                return getBean(entry.getKey());
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        Object bean = getBeanByType(requiredType);
        if (bean == null) {
            log.error("未找到类型为: {} 的Bean", requiredType.getName());
            throw new RuntimeException("未找到类型为: " + requiredType.getName());
        }
        return (T) bean;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> requiredType){
        Object bean = getBean(name);
        if (!requiredType.isInstance(bean)) {
            log.error("Bean {} 不是 {} 类型", name, requiredType.getName());
            throw new RuntimeException("Bean  不是" + name + " 类型 " + requiredType.getName());
        }
        return (T) bean;
    }

    @Override
    public boolean containsBean(String name) {
        return beanDefinitionMap.containsKey(name);
    }

    @Override
    public boolean isSingleton(String name) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        return beanDefinition != null && beanDefinition.isSingleton();
    }

    @Override
    public Class<?> getType(String name) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        return beanDefinition != null ? beanDefinition.getBeanClass() : null;
    }
}