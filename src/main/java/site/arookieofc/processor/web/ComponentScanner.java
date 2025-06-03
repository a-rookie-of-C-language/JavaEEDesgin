package site.arookieofc.processor.web;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.annotation.web.Controller;
import site.arookieofc.processor.config.ConfigProcessor;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ComponentScanner {
    
    @Config("web.controller")
    private static String controllerPackage;
    
    static {
        controllerPackage = ConfigProcessor.getStringValue("web.controller", "site.arookieofc.controller");
        log.info("控制器包路径配置为: {}", controllerPackage);
    }
    
    public static void scanAndRegisterControllers() {
        log.info("开始扫描并注册控制器...");
        try {
            registerControllers(controllerPackage);
            log.info("控制器扫描注册完成");
        } catch (Exception e) {
            log.error("扫描注册控制器时发生错误: {}", e.getMessage(), e);
        }
    }

    private static void registerControllers(String controllerPackage) {
        log.debug("开始扫描包: {}", controllerPackage);
        List<Class<?>> classes = scanPackage(controllerPackage);
        log.debug("在包 {} 中找到 {} 个类", controllerPackage, classes.size());
        
        int controllerCount = 0;
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                log.debug("注册控制器: {}", clazz.getName());
                HttpMappingProcessor.registerController(clazz);
                controllerCount++;
            }
        }
        log.info("共注册了 {} 个控制器", controllerCount);
    }
    
    private static List<Class<?>> scanPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(packagePath);
        
        if (resource != null) {
            File directory = new File(resource.getFile());
            if (directory.exists() && directory.isDirectory()) {
                log.debug("扫描目录: {}", directory.getAbsolutePath());
                scanDirectory(directory, packageName, classes);
            } else {
                log.warn("包路径不存在或不是目录: {}", packagePath);
            }
        } else {
            log.warn("无法获取包资源: {}", packagePath);
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
                        log.trace("尝试加载类: {}", className);
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        log.debug("无法加载类: {}, 原因: {}", className, e.getMessage());
                    }
                }
            }
        }
    }
}