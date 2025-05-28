package site.arookieofc.processor;

import lombok.AllArgsConstructor;
import site.arookieofc.annotation.web.*;
import com.fasterxml.jackson.databind.ObjectMapper;

// 改为jakarta.servlet
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class HttpMappingProcessor extends HttpServlet {
    private static final Map<String, MethodInfo> getMappings = new HashMap<>();
    private static final Map<String, MethodInfo> postMappings = new HashMap<>();
    private static final Map<String, MethodInfo> putMappings = new HashMap<>();
    private static final Map<String, MethodInfo> deleteMappings = new HashMap<>();
    
    // 添加Jackson ObjectMapper用于JSON序列化
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
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
        // 修改路径获取逻辑
        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            path = path.substring(contextPath.length());
        }
        
        System.out.println("Processing request: " + req.getMethod() + " " + path); // 添加调试日志
        
        // 尝试精确匹配
        MethodInfo methodInfo = mappings.get(path);
        
        // 如果精确匹配失败，尝试路径变量匹配
        if (methodInfo == null) {
            methodInfo = findMethodWithPathVariables(path, mappings);
        }
        
        if (methodInfo != null) {
            try {
                Object controller = methodInfo.controllerClass.newInstance();
                
                // 设置CORS响应头
                resp.setHeader("Access-Control-Allow-Origin", "*");
                resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
                
                // 设置响应头
                resp.setContentType("application/json;charset=UTF-8");
                resp.setCharacterEncoding("UTF-8");
                
                // 实现参数注入逻辑
                Object[] args = buildMethodArguments(methodInfo.method, req, path);
                Object result = methodInfo.method.invoke(controller, args);
                
                if (result != null) {
                    // 使用Jackson自动序列化为JSON
                    String jsonResponse;
                    if (result instanceof String) {
                        // 如果返回值已经是字符串，检查是否已经是JSON格式
                        String resultStr = (String) result;
                        if (isValidJson(resultStr)) {
                            jsonResponse = resultStr;
                        } else {
                            // 如果不是JSON格式，将字符串作为JSON字符串值
                            jsonResponse = objectMapper.writeValueAsString(resultStr);
                        }
                    } else {
                        // 对于其他类型的对象，自动序列化为JSON
                        jsonResponse = objectMapper.writeValueAsString(result);
                    }
                    resp.getWriter().write(jsonResponse);
                } else {
                    // 返回null时，返回空JSON对象
                    resp.getWriter().write("{}");
                }
            } catch (Exception e) {
                e.printStackTrace(); // 添加详细错误日志
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
            }
        } else {
            System.out.println("No mapping found for path: " + path); // 添加调试日志
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "404 Not Found");
                errorResponse.put("path", path);
                resp.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            } catch (Exception e) {
                resp.getWriter().write("{\"error\":\"404 Not Found\"}");
            }
        }
    }
    
    // 添加辅助方法检查字符串是否为有效JSON
    private boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private MethodInfo findMethodWithPathVariables(String requestPath, Map<String, MethodInfo> mappings) {
        for (Map.Entry<String, MethodInfo> entry : mappings.entrySet()) {
            String mappingPath = entry.getKey();
            if (pathMatches(requestPath, mappingPath)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    private boolean pathMatches(String requestPath, String mappingPath) {
        // 简单的路径变量匹配逻辑
        String[] requestSegments = requestPath.split("/");
        String[] mappingSegments = mappingPath.split("/");
        
        if (requestSegments.length != mappingSegments.length) {
            return false;
        }
        
        for (int i = 0; i < requestSegments.length; i++) {
            String mappingSegment = mappingSegments[i];
            if (!mappingSegment.startsWith("{") && !mappingSegment.equals(requestSegments[i])) {
                return false;
            }
        }
        return true;
    }
    
    private static void scanAndRegisterMappings() {
        // 使用新的组件扫描器自动扫描和注册控制器
        ComponentScanner.scanAndRegisterControllers();
    }
    
    public static void registerController(Class<?> controllerClass) {
        if (!controllerClass.isAnnotationPresent(Controller.class)) {
            return;
        }
        
        Controller controllerAnnotation = controllerClass.getAnnotation(Controller.class);
        String basePath = controllerAnnotation.value();
        
        System.out.println("Registering controller: " + controllerClass.getName() + " with base path: " + basePath);
        
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            registerMethodMapping(method, controllerClass, basePath, GetMapping.class, getMappings);
            registerMethodMapping(method, controllerClass, basePath, PostMapping.class, postMappings);
            registerMethodMapping(method, controllerClass, basePath, PutMapping.class, putMappings);
            registerMethodMapping(method, controllerClass, basePath, DeleteMapping.class, deleteMappings);
        }
        
        // 打印所有注册的路由
        System.out.println("GET mappings: " + getMappings.keySet());
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
    @AllArgsConstructor
    private static class MethodInfo {
        final Method method;
        final Class<?> controllerClass;

    }

    private Object[] buildMethodArguments(Method method, HttpServletRequest req, String requestPath) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();
            
            // 处理@RequestParam
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String paramValue = req.getParameter(requestParam.value());
                if (paramValue == null && requestParam.required()) {
                    throw new IllegalArgumentException("Required parameter '" + requestParam.value() + "' is missing");
                }
                if (paramValue == null && !requestParam.defaultValue().isEmpty()) {
                    paramValue = requestParam.defaultValue();
                }
                args[i] = convertStringToType(paramValue, paramType);
                continue;
            }
            
            // 处理@PathVariable
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String variableValue = extractPathVariable(requestPath, pathVariable.value());
                args[i] = convertStringToType(variableValue, paramType);
                continue;
            }
            
            // 处理@RequestBody
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                String jsonBody = getRequestBody(req);
                args[i] = objectMapper.readValue(jsonBody, paramType);
                continue;
            }
            
            // 默认为null
            args[i] = null;
        }
        
        return args;
    }

    private Object convertStringToType(String value, Class<?> targetType) {
        if (value == null) return null;
        
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private String extractPathVariable(String requestPath, String variableName) {
        // 简化的路径变量提取逻辑
        String[] pathParts = requestPath.split("/");
        // 这里需要根据具体的路径模式来提取变量
        // 暂时返回最后一个路径段
        return pathParts[pathParts.length - 1];
    }

    private String getRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader = req.getReader();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}