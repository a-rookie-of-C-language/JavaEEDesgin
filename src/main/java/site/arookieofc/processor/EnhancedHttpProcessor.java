package site.arookieofc.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import site.arookieofc.annotation.web.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnhancedHttpProcessor extends HttpServlet {
    private static final Map<String, RouteInfo> getMappings = new HashMap<>();
    private static final Map<String, RouteInfo> postMappings = new HashMap<>();
    private static final Map<String, RouteInfo> putMappings = new HashMap<>();
    private static final Map<String, RouteInfo> deleteMappings = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        scanAndRegisterMappings();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, getMappings);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, postMappings);
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, putMappings);
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, deleteMappings);
    }
    
    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, Map<String, RouteInfo> mappings) throws IOException {
        String path = getRequestPath(req);
        
        RouteInfo routeInfo = findMatchingRoute(path, mappings);
        if (routeInfo != null) {
            try {
                Object controller = routeInfo.controllerClass.newInstance();
                Object[] args = buildMethodArguments(routeInfo, req, resp, path);
                Object result = routeInfo.method.invoke(controller, args);
                
                // 自动处理响应 - 如果方法有HTTP映射注解，自动当作@ResponseBody处理
                handleResponse(result, routeInfo.method, resp);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\":\"404 Not Found\"}");
        }
    }
    
    private String getRequestPath(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null) {
            path = req.getServletPath();
        }
        return path;
    }
    
    private RouteInfo findMatchingRoute(String requestPath, Map<String, RouteInfo> mappings) {
        // 先尝试精确匹配
        RouteInfo exactMatch = mappings.get(requestPath);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        // 尝试路径变量匹配
        for (Map.Entry<String, RouteInfo> entry : mappings.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.contains("{") && pattern.contains("}")) {
                String regex = pattern.replaceAll("\\{[^}]+}", "([^/]+)");
                if (requestPath.matches(regex)) {
                    return entry.getValue();
                }
            }
        }
        
        return null;
    }
    
    private Object[] buildMethodArguments(RouteInfo routeInfo, HttpServletRequest req, HttpServletResponse resp, String requestPath) throws Exception {
        Parameter[] parameters = routeInfo.method.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();
            
            // 处理HttpServletRequest和HttpServletResponse
            if (paramType == HttpServletRequest.class) {
                args[i] = req;
            } else if (paramType == HttpServletResponse.class) {
                args[i] = resp;
            } else {
                args[i] = resolveParameter(parameter, req, requestPath, routeInfo.pathPattern, routeInfo.httpMethod);
            }
        }
        
        return args;
    }
    
    private Object resolveParameter(Parameter parameter, HttpServletRequest req, String requestPath, String pathPattern, String httpMethod) throws Exception {
        Class<?> paramType = parameter.getType();
        
        // 处理@RequestParam
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null) {
            String paramValue = req.getParameter(requestParam.value());
            if (paramValue == null && requestParam.required()) {
                throw new IllegalArgumentException("Required parameter '" + requestParam.value() + "' is missing");
            }
            if (paramValue == null) {
                paramValue = requestParam.defaultValue();
            }
            return convertStringToType(paramValue, paramType);
        }
        
        // 处理@PathVariable
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (pathVariable != null) {
            String variableValue = extractPathVariable(requestPath, pathPattern, pathVariable.value());
            return convertStringToType(variableValue, paramType);
        }
        
        // 处理@RequestBody或自动判断是否需要从请求体解析
        RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
        if (requestBody != null || shouldParseFromRequestBody(httpMethod, paramType)) {
            String jsonBody = getRequestBody(req);
            if (!jsonBody.trim().isEmpty()) {
                return objectMapper.readValue(jsonBody, paramType);
            } else {
                // 如果没有请求体，尝试从表单参数构建对象
                return buildObjectFromFormParams(req, paramType);
            }
        }
        
        return null;
    }
    
    // 自动判断是否应该从请求体解析（类似Spring的自动处理）
    private boolean shouldParseFromRequestBody(String httpMethod, Class<?> paramType) {
        // 对于POST和PUT请求，如果参数是复杂对象（非基本类型），自动从请求体解析
        return ("POST".equals(httpMethod) || "PUT".equals(httpMethod)) &&
                !isSimpleType(paramType);
    }
    
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || 
               type == String.class || 
               type == Integer.class || 
               type == Long.class || 
               type == Boolean.class || 
               type == Double.class || 
               type == Float.class;
    }
    
    private Object buildObjectFromFormParams(HttpServletRequest req, Class<?> paramType) throws Exception {
        // 这里可以通过反射设置字段值，简化实现
        return paramType.newInstance();
    }
    
    private String extractPathVariable(String requestPath, String pathPattern, String variableName) {
        String regex = pathPattern.replaceAll("\\{([^}]+)}", "(?<$1>[^/]+)");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(requestPath);
        
        if (matcher.matches()) {
            try {
                return matcher.group(variableName);
            } catch (IllegalArgumentException e) {
                String[] variables = extractVariableNames(pathPattern);
                for (int i = 0; i < variables.length; i++) {
                    if (variables[i].equals(variableName)) {
                        return matcher.group(i + 1);
                    }
                }
            }
        }
        
        return null;
    }
    
    private String[] extractVariableNames(String pathPattern) {
        Pattern pattern = Pattern.compile("\\{([^}]+)}");
        Matcher matcher = pattern.matcher(pathPattern);
        java.util.List<String> variables = new java.util.ArrayList<>();
        
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return variables.toArray(new String[0]);
    }
    
    private String getRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
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
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }
        
        return value;
    }
    
    private void handleResponse(Object result, Method method, HttpServletResponse resp) throws IOException {
        if (result == null) {
            return;
        }
        
        // 自动处理响应：如果方法有HTTP映射注解，自动当作@ResponseBody处理
        boolean hasHttpMapping = method.isAnnotationPresent(GetMapping.class) ||
                               method.isAnnotationPresent(PostMapping.class) ||
                               method.isAnnotationPresent(PutMapping.class) ||
                               method.isAnnotationPresent(DeleteMapping.class);
        
        if (hasHttpMapping) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(objectMapper.writeValueAsString(result));
        } else {
            resp.getWriter().write(result.toString());
        }
    }
    
    private static void scanAndRegisterMappings() {
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
            registerMethodMapping(method, controllerClass, basePath, GetMapping.class, getMappings, "GET");
            registerMethodMapping(method, controllerClass, basePath, PostMapping.class, postMappings, "POST");
            registerMethodMapping(method, controllerClass, basePath, PutMapping.class, putMappings, "PUT");
            registerMethodMapping(method, controllerClass, basePath, DeleteMapping.class, deleteMappings, "DELETE");
        }
    }
    
    private static void registerMethodMapping(Method method, Class<?> controllerClass, String basePath,
                                            Class<? extends Annotation> annotationClass,
                                            Map<String, RouteInfo> mappings, String httpMethod) {
        if (method.isAnnotationPresent(annotationClass)) {
            try {
                Annotation annotation = method.getAnnotation(annotationClass);
                Method valueMethod = annotationClass.getMethod("value");
                String path = (String) valueMethod.invoke(annotation);
                
                String fullPath = basePath.isEmpty() ? path : basePath + path;
                if (!fullPath.startsWith("/")) {
                    fullPath = "/" + fullPath;
                }
                
                mappings.put(fullPath, new RouteInfo(method, controllerClass, fullPath, httpMethod));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @AllArgsConstructor
    private static class RouteInfo {
        final Method method;
        final Class<?> controllerClass;
        final String pathPattern;
        final String httpMethod;
    }
}