package site.arookieofc.processor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Config;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ConfigProcessor {
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static Map<String, Object> configMap;
    
    static {
        log.info("初始化配置处理器...");
        loadConfig();
        autoInjectAllClasses();
        log.info("配置处理器初始化完成");
    }
    
    private static void autoInjectAllClasses() {
        log.debug("开始自动注入所有类的配置...");
        try {
            String basePackage = "site.arookieofc";
            Set<Class<?>> classes = scanClassesWithConfigAnnotation(basePackage);
            log.debug("找到 {} 个带有配置注解的类", classes.size());
            
            for (Class<?> clazz : classes) {
                log.trace("注入类 {} 的配置", clazz.getName());
                injectStaticFields(clazz);
            }
            log.debug("配置自动注入完成");
        } catch (Exception e) {
            log.error("自动注入配置时发生错误: {}", e.getMessage(), e);
        }
    }
    
    private static Set<Class<?>> scanClassesWithConfigAnnotation(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            String path = basePackage.replace('.', '/');
            URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            if (resource != null) {
                File directory = new File(resource.getFile());
                scanDirectory(directory, basePackage, classes);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return classes;
    }
    
    private static void scanDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) return;
        
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (hasConfigAnnotatedFields(clazz)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // 忽略无法加载的类
                }
            }
        }
    }
    
    private static boolean hasConfigAnnotatedFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Config.class)) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private static void loadConfig() {
        log.debug("加载配置文件...");
        try {
            InputStream inputStream = ConfigProcessor.class.getClassLoader().getResourceAsStream("config.yml");
            if (inputStream != null) {
                configMap = yamlMapper.readValue(inputStream, Map.class);
                log.info("成功加载配置文件");
                if (log.isDebugEnabled()) {
                    log.debug("配置项数量: {}", configMap.size());
                }
            } else {
                log.error("在资源目录中未找到config.yml");
                throw new RuntimeException("config.yml not found in resources directory");
            }
        } catch (Exception e) {
            log.error("加载config.yml失败: {}", e.getMessage(), e);
        }
    }

    public static Object getConfigValue(String keyPath) {
        log.trace("获取配置值: {}", keyPath);
        String[] keys = keyPath.split("\\.");
        Object current = configMap;
        
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else {
                log.debug("配置路径 {} 中的键 {} 不存在", keyPath, key);
                return null;
            }
        }
        
        return current;
    }

    public static String getStringValue(String keyPath, String defaultValue) {
        Object value = getConfigValue(keyPath);
        String result = value != null ? value.toString() : defaultValue;
        log.trace("获取字符串配置: {}, 值: {}", keyPath, result);
        return result;
    }

    public static void injectStaticFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Config.class) && 
                java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                
                Config configAnnotation = field.getAnnotation(Config.class);
                String keyPath = configAnnotation.value();
                String defaultValue = configAnnotation.defaultValue();
                boolean required = configAnnotation.required();
                
                try {
                    field.setAccessible(true);
                    Object configValue = getConfigValue(keyPath);
                    
                    if (configValue == null) {
                        if (!defaultValue.isEmpty()) {
                            configValue = defaultValue;
                        } else if (required) {
                            log.error("Required config property '{}' not found", keyPath);
                        }
                    }
                    
                    if (configValue != null) {
                        Object convertedValue = convertValue(configValue, field.getType());
                        field.set(null, convertedValue);
                    }
                } catch (IllegalAccessException e) {
                    log.error("Failed to inject config for static field: {}", field.getName(), e);
                }
            }
        }
    }
    
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        String stringValue = value.toString();
        
        if (targetType == String.class) {
            return stringValue;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(stringValue);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(stringValue);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(stringValue);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(stringValue);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(stringValue);
        }
        
        return value;
    }
}