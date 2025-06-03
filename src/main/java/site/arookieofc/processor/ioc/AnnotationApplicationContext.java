package site.arookieofc.processor.ioc;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.Main;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Bean;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.processor.sql.SQLExecutor;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AnnotationApplicationContext implements ApplicationContext {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    // 一级缓存：完成品单例对象
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    // 二级缓存：早期单例对象（已实例化但未完成依赖注入）
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    // 三级缓存：单例工厂（用于创建早期对象）
    private final Map<String, ObjectFactory> singletonFactories = new ConcurrentHashMap<>();
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
        // 处理@Component注解
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getAnnotation(Component.class);
            String beanName = component.value().isEmpty() ?
                    clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1) :
                    component.value();
            BeanDefinition beanDefinition = new BeanDefinition(beanName, clazz);
            beanDefinitionMap.put(beanName, beanDefinition);
            processBeanMethods(clazz);
        }
    }
    private void processBeanMethods(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Bean.class)) {
                Bean bean = method.getAnnotation(Bean.class);
                String beanName = bean.value().isEmpty() ? method.getName() : bean.value();
                BeanDefinition beanDefinition = new BeanDefinition(
                        beanName,
                        method.getReturnType(),
                        method,
                        null
                );
                beanDefinitionMap.put(beanName, beanDefinition);
            }
        }
    }

    private void preInstantiateSingletons() {
        List<String> beanNames = new ArrayList<>(beanDefinitionMap.keySet());
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.isSingleton()) {
                getBean(beanName);
            }
        }
    }

    @Override
    public Object getBean(String name){
        return doGetBean(name);
    }

    private Object doGetBean(String name){
        // 先从三级缓存中获取单例对象
        Object singleton = getSingleton(name);
        if (singleton != null) {
            return singleton;
        }

        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        if (beanDefinition == null) {
            log.error("未找到Bean定义: {}, {}", name, beanDefinitionMap);
            throw new RuntimeException("未找到Bean定义: " + name);
        }

        // 如果是单例Bean
        if (beanDefinition.isSingleton()) {
            singleton = getSingleton(name, () -> {
                try {
                    return createBean(name, beanDefinition);
                } catch (Exception e) {
                    log.error("创建Bean失败: {}, {}", name, beanDefinition, e);
                    throw new RuntimeException("创建Bean失败: " + name, e);
                }
            });
            return singleton;
        } else {
            try {
                return createBean(name, beanDefinition);
            } catch (Exception e) {
                log.error("创建Bean失败: {}, {}", name, beanDefinition, e);
                throw new RuntimeException("创建Bean失败: " + name, e);
            }
        }
    }

    /**
     * 从三级缓存中获取单例对象
     */
    private Object getSingleton(String beanName)  {
        // 一级缓存
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (singletonObjects) {
                // 二级缓存
                singletonObject = earlySingletonObjects.get(beanName);
                if (singletonObject == null) {
                    // 三级缓存
                    ObjectFactory singletonFactory = singletonFactories.get(beanName);
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
    private Object getSingleton(String beanName, ObjectFactory singletonFactory) {
        synchronized (singletonObjects) {
            Object singletonObject = singletonObjects.get(beanName);
            if (singletonObject == null) {
                beforeSingletonCreation(beanName);
                try {
                    singletonObject = singletonFactory.getObject();
                    addSingleton(beanName, singletonObject);
                } finally {
                    afterSingletonCreation(beanName);
                }
            }
            return singletonObject;
        }
    }

    /**
     * 单例创建前的准备工作
     */
    private void beforeSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.add(beanName)) {
            log.error("检测到循环依赖:: {}, {}", beanName, singletonsCurrentlyInCreation);
            throw new RuntimeException("检测到循环依赖: " + beanName);
        }
    }

    /**
     * 单例创建后的清理工作
     */
    private void afterSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.remove(beanName)) {
            log.error("单例 : {}, {} 不在创建列表中", beanName, singletonsCurrentlyInCreation);
            throw new IllegalStateException("单例 " + beanName + " 不在创建列表中");
        }
    }

    private boolean isSingletonCurrentlyInCreation(String beanName) {
        return singletonsCurrentlyInCreation.contains(beanName);
    }

    /**
     * 将完成的单例对象添加到一级缓存
     */
    private void addSingleton(String beanName, Object singletonObject) {
        synchronized (singletonObjects) {
            singletonObjects.put(beanName, singletonObject);
            singletonFactories.remove(beanName);
            earlySingletonObjects.remove(beanName);
        }
    }

    /**
     * 添加单例工厂到三级缓存
     */
    private void addSingletonFactory(String beanName, ObjectFactory singletonFactory) {
        synchronized (singletonObjects) {
            if (!singletonObjects.containsKey(beanName)) {
                singletonFactories.put(beanName, singletonFactory);
                earlySingletonObjects.remove(beanName);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Object instance;
        if (beanDefinition.getFactoryMethod() != null) {
            instance = createBeanByFactoryMethod(beanDefinition);
        } else {
            try {
                instance = beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
            }catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("createBean error: {}, {}", beanName, beanDefinition, e);
                throw new RuntimeException("createBean error: " + beanName, e);
            }

        }

        // 如果是单例Bean，提前暴露到三级缓存中
        if (beanDefinition.isSingleton()) {
            final Object finalInstance = instance;
            addSingletonFactory(beanName, () -> getEarlyBeanReference(finalInstance));
        }
        if (instance != null) {
            populateBean(instance);
        }
        return instance;
    }

    /**
     * 获取早期Bean引用（用于解决循环依赖）
     */
    private Object getEarlyBeanReference(Object bean) {
        return bean;
    }

    private void populateBean(Object instance) {
        Class<?> clazz = instance.getClass();
        injectFields(instance, clazz);
        injectMethods(instance, clazz);
    }

    private void injectFields(Object instance, Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                try {
                    field.setAccessible(true);
                    Object dependency = getBeanByType(field.getType());
                    if (dependency != null) {
                        field.set(instance, dependency);
                    } else {
                        String fieldName = field.getName();
                        if (containsBean(fieldName)) {
                            dependency = getBean(fieldName);
                            field.set(instance, dependency);
                        } else if (autowired.required()) {
                            log.error("无法找到依赖: {}", field.getType().getName());
                            throw new RuntimeException("无法找到依赖: " + field.getType().getName());
                        }
                    }
                } catch (Exception e) {
                    if (autowired.required()) {
                        log.error("依赖注入失败: {}", field.getName(),e);
                        throw new RuntimeException("依赖注入失败: " + field.getName(), e);
                    }
                }
            }
        }
    }

    private void injectMethods(Object instance, Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(Autowired.class)) {
                Autowired autowired = method.getAnnotation(Autowired.class);
                try {
                    method.setAccessible(true);
                    Class<?>[] paramTypes = method.getParameterTypes();
                    Object[] args = new Object[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        Object dependency = getBeanByType(paramTypes[i]);
                        if (dependency == null && autowired.required()) {
                            log.error("依赖注入失败: {}", paramTypes[i].getName());
                            throw new RuntimeException("无法找到方法参数依赖: " + paramTypes[i].getName());
                        }
                        args[i] = dependency;
                    }

                    method.invoke(instance, args);
                } catch (Exception e) {
                    if (autowired.required()) {
                        log.error("方法依赖注入失败: {}", method.getName(),e);
                        throw new RuntimeException("方法依赖注入失败: " + method.getName(), e);
                    }
                }
            }
        }
    }
    private Object createBeanByFactoryMethod(BeanDefinition beanDefinition) {
        Method factoryMethod = beanDefinition.getFactoryMethod();
        Object factoryBean = beanDefinition.getFactoryBean();

        if (factoryBean == null) {
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
        }catch (NoSuchMethodError | IllegalAccessException | InvocationTargetException e){
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

