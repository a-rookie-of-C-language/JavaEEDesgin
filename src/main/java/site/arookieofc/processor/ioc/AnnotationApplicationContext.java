package site.arookieofc.processor.ioc;

import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Bean;
import site.arookieofc.annotation.ioc.Component;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationApplicationContext implements ApplicationContext {
    
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Set<String> creatingSingletons = Collections.synchronizedSet(new HashSet<>());
    
    private final String[] basePackages;
    
    public AnnotationApplicationContext(String... basePackages) {
        this.basePackages = basePackages;
        refresh();
    }
    
    @Override
    public void refresh() {
        // 1. 扫描组件
        scanComponents();
        
        // 2. 预实例化单例Bean
        preInstantiateSingletons();
    }
    
    private void scanComponents() {
        for (String basePackage : basePackages) {
            scanPackage(basePackage);
        }
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
            System.err.println("扫描包失败: " + packageName + ", " + e.getMessage());
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
                
                BeanDefinition beanDefinition = new BeanDefinition(
                    beanName, 
                    method.getReturnType(), 
                    method, 
                    null // 工厂Bean实例稍后设置
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
    public Object getBean(String name) {
        // 先从单例缓存中获取
        Object singleton = singletonObjects.get(name);
        if (singleton != null) {
            return singleton;
        }
        
        // 检查是否正在创建中（解决循环依赖）
        if (creatingSingletons.contains(name)) {
            throw new RuntimeException("检测到循环依赖: " + name);
        }
        
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        if (beanDefinition == null) {
            throw new RuntimeException("未找到Bean定义: " + name);
        }
        
        return createBean(name, beanDefinition);
    }
    
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        creatingSingletons.add(beanName);
        
        try {
            Object instance;
            
            // 如果是通过@Bean方法创建
            if (beanDefinition.getFactoryMethod() != null) {
                instance = createBeanByFactoryMethod(beanDefinition);
            } else {
                // 通过构造函数创建
                instance = beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
            }
            
            // 依赖注入
            populateBean(instance);
            
            // 如果是单例，缓存起来
            if (beanDefinition.isSingleton()) {
                singletonObjects.put(beanName, instance);
                beanDefinition.setInstance(instance);
            }
            
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("创建Bean失败: " + beanName, e);
        } finally {
            creatingSingletons.remove(beanName);
        }
    }
    
    private Object createBeanByFactoryMethod(BeanDefinition beanDefinition) throws Exception {
        Method factoryMethod = beanDefinition.getFactoryMethod();
        Object factoryBean = beanDefinition.getFactoryBean();
        
        // 如果工厂Bean还没有实例化，先实例化它
        if (factoryBean == null) {
            // 找到包含这个@Bean方法的类的Bean定义
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
        return factoryMethod.invoke(factoryBean);
    }
    
    private void populateBean(Object instance) {
        Class<?> clazz = instance.getClass();
        
        // 字段注入
        injectFields(instance, clazz);
        
        // 方法注入
        injectMethods(instance, clazz);
    }
    
    private void injectFields(Object instance, Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                try {
                    field.setAccessible(true);
                    
                    // 先按类型查找
                    Object dependency = getBeanByType(field.getType());
                    if (dependency != null) {
                        field.set(instance, dependency);
                    } else {
                        // 按名称查找
                        String fieldName = field.getName();
                        if (containsBean(fieldName)) {
                            dependency = getBean(fieldName);
                            field.set(instance, dependency);
                        } else if (autowired.required()) {
                            throw new RuntimeException("无法找到依赖: " + field.getType().getName());
                        }
                        // 如果required=false且找不到依赖，则跳过注入
                    }
                } catch (Exception e) {
                    if (autowired.required()) {
                        throw new RuntimeException("依赖注入失败: " + field.getName(), e);
                    }
                    // 如果required=false，忽略注入失败
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
                            throw new RuntimeException("无法找到方法参数依赖: " + paramTypes[i].getName());
                        }
                        args[i] = dependency;
                    }
                    
                    method.invoke(instance, args);
                } catch (Exception e) {
                    if (autowired.required()) {
                        throw new RuntimeException("方法依赖注入失败: " + method.getName(), e);
                    }
                }
            }
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
            throw new RuntimeException("未找到类型为 " + requiredType.getName() + " 的Bean");
        }
        return (T) bean;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> requiredType) {
        Object bean = getBean(name);
        if (!requiredType.isInstance(bean)) {
            throw new RuntimeException("Bean " + name + " 不是 " + requiredType.getName() + " 类型");
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
    
    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }
    
    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            if (type.isAssignableFrom(entry.getValue().getBeanClass())) {
                result.add(entry.getKey());
            }
        }
        return result.toArray(new String[0]);
    }
    
    @Override
    public void close() {
        singletonObjects.clear();
        beanDefinitionMap.clear();
        creatingSingletons.clear();
    }
}