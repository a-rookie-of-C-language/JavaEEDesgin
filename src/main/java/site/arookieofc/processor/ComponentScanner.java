package site.arookieofc.processor;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Application;
import site.arookieofc.annotation.config.ComponentScan;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.annotation.web.Controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
@Slf4j
public class ComponentScanner {
    
    @Config("web.controller")
    private static String controllerPackage;
    
    static {
        controllerPackage = ConfigProcessor.getStringValue("web.controller", "site.arookieofc.controller");
    }
    
    public static void scanAndRegisterControllers() {
        try {
            registerControllers(controllerPackage);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void registerControllers(String controllerPackage) {
        List<Class<?>> classes = scanPackage(controllerPackage);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                HttpMappingProcessor.registerController(clazz);
                System.out.println("Registered controller: " + clazz.getName());
            }
        }
    }

    public static void scanAndRegisterControllers(Class<?> applicationClass) {
        try {
            List<String> packagesToScan = new ArrayList<>();
            if (applicationClass.isAnnotationPresent(Application.class)) {
                Application app = applicationClass.getAnnotation(Application.class);
                ComponentScan componentScan = app.annotationType().getAnnotation(ComponentScan.class);
                if (componentScan != null) {
                    if (componentScan.value().length == 0 && componentScan.basePackages().length == 0) {
                        packagesToScan.add(applicationClass.getPackage().getName());
                    } else {
                        packagesToScan.addAll(Arrays.asList(componentScan.value()));
                        Collections.addAll(packagesToScan, componentScan.basePackages());
                    }
                }
            }

            if (applicationClass.isAnnotationPresent(ComponentScan.class)) {
                ComponentScan componentScan = applicationClass.getAnnotation(ComponentScan.class);
                packagesToScan.addAll(Arrays.asList(componentScan.value()));
                packagesToScan.addAll(Arrays.asList(componentScan.basePackages()));
            }

            if (packagesToScan.isEmpty()) {
                packagesToScan.add(applicationClass.getPackage().getName());
            }

            for (String packageName : packagesToScan) {
                registerControllers(packageName);
            }
        } catch (Exception e) {
           log.error(e.getMessage(), e);
        }
    }
    
    private static List<Class<?>> scanPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(packagePath);
        
        if (resource != null) {
            File directory = new File(resource.getFile());
            if (directory.exists() && directory.isDirectory()) {
                scanDirectory(directory, packageName, classes);
            }
        }
        
        return classes;
    }
    
    private static void scanDirectory(File directory, String packageName, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName(), classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        // 忽略无法加载的类
                    }
                }
            }
        }
    }
}