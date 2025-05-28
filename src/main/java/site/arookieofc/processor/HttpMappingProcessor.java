package site.arookieofc.processor;

import site.arookieofc.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HttpMappingProcessor extends HttpServlet {
    private static final Map<String, MethodInfo> getMappings = new HashMap<>();
    private static final Map<String, MethodInfo> postMappings = new HashMap<>();
    private static final Map<String, MethodInfo> putMappings = new HashMap<>();
    private static final Map<String, MethodInfo> deleteMappings = new HashMap<>();
    
    static {
        // 在静态块中扫描并注册所有带注解的方法
        scanAndRegisterMappings();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, getMappings);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, postMappings);
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, putMappings);
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, deleteMappings);
    }
    
    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, Map<String, MethodInfo> mappings) throws IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = req.getServletPath();
        }
        
        MethodInfo methodInfo = mappings.get(path);
        if (methodInfo != null) {
            try {
                Object controller = methodInfo.controllerClass.newInstance();
                Object result = methodInfo.method.invoke(controller, req, resp);
                
                if (result != null) {
                    resp.getWriter().write(result.toString());
                }
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Internal Server Error: " + e.getMessage());
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("404 Not Found");
        }
    }
    
    private static void scanAndRegisterMappings() {
        // 这里需要扫描所有带@Controller注解的类
        // 由于Java反射的限制，这里提供一个简化的注册方法
        // 实际项目中可以使用类路径扫描或配置文件
        
        // 示例：手动注册StudentController
        try {
            Class<?> studentControllerClass = Class.forName("site.arookieofc.controller.StudentController");
            registerController(studentControllerClass);
        } catch (ClassNotFoundException e) {
            // 忽略未找到的类
        }
    }
    
    public static void registerController(Class<?> controllerClass) {
        if (!controllerClass.isAnnotationPresent(Controller.class)) {
            return;
        }
        
        Controller controllerAnnotation = controllerClass.getAnnotation(Controller.class);
        String basePath = controllerAnnotation.value();
        
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            registerMethodMapping(method, controllerClass, basePath, GetMapping.class, getMappings);
            registerMethodMapping(method, controllerClass, basePath, PostMapping.class, postMappings);
            registerMethodMapping(method, controllerClass, basePath, PutMapping.class, putMappings);
            registerMethodMapping(method, controllerClass, basePath, DeleteMapping.class, deleteMappings);
        }
    }
    
    private static void registerMethodMapping(Method method, Class<?> controllerClass, String basePath, 
                                            Class<? extends java.lang.annotation.Annotation> annotationClass, 
                                            Map<String, MethodInfo> mappings) {
        if (method.isAnnotationPresent(annotationClass)) {
            try {
                java.lang.annotation.Annotation annotation = method.getAnnotation(annotationClass);
                Method valueMethod = annotationClass.getMethod("value");
                String path = (String) valueMethod.invoke(annotation);
                
                String fullPath = basePath.isEmpty() ? path : basePath + path;
                if (!fullPath.startsWith("/")) {
                    fullPath = "/" + fullPath;
                }
                
                mappings.put(fullPath, new MethodInfo(method, controllerClass));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static class MethodInfo {
        final Method method;
        final Class<?> controllerClass;
        
        MethodInfo(Method method, Class<?> controllerClass) {
            this.method = method;
            this.controllerClass = controllerClass;
        }
    }
}