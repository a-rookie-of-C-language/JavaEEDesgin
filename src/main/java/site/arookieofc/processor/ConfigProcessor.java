package site.arookieofc.processor;

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
        loadConfig();
        autoInjectAllClasses();
    }
    
    private static void autoInjectAllClasses() {
        try {
            String basePackage = "site.arookieofc";
            Set<Class<?>> classes = scanClassesWithConfigAnnotation(basePackage);
            
            for (Class<?> clazz : classes) {
                injectStaticFields(clazz);
            }
        } catch (Exception e) {
            System.err.println("Auto injection failed: " + e.getMessage());
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
            log.info(e.getMessage());
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
        try {
            InputStream inputStream = ConfigProcessor.class.getClassLoader().getResourceAsStream("config.yml");
            if (inputStream != null) {
                configMap = yamlMapper.readValue(inputStream, Map.class);
            } else {
                throw new RuntimeException("config.yml not found in resources directory");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.yml", e);
        }
    }

    public static Object getConfigValue(String keyPath) {
        String[] keys = keyPath.split("\\.");
        Object current = configMap;
        
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else {
                return null;
            }
        }
        
        return current;
    }

    public static String getStringValue(String keyPath, String defaultValue) {
        Object value = getConfigValue(keyPath);
        return value != null ? value.toString() : defaultValue;
    }

    public static int getIntValue(String keyPath, int defaultValue) {
        Object value = getConfigValue(keyPath);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static void injectConfig(Object target) {
        Class<?> clazz = target.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Config.class)) {
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
                            throw new RuntimeException("Required config property '" + keyPath + "' not found");
                        }
                    }
                    if (configValue != null) {
                        Object convertedValue = convertValue(configValue, field.getType());
                        field.set(target, convertedValue);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject config for field: " + field.getName(), e);
                }
            }
        }
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
                            throw new RuntimeException("Required config property '" + keyPath + "' not found");
                        }
                    }
                    
                    if (configValue != null) {
                        Object convertedValue = convertValue(configValue, field.getType());
                        field.set(null, convertedValue);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject config for static field: " + field.getName(), e);
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