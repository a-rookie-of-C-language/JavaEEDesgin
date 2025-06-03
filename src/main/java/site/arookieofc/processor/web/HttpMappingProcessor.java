package site.arookieofc.processor.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.web.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import reactor.core.publisher.Flux;
import java.io.PrintWriter;

@Slf4j
public class HttpMappingProcessor extends HttpServlet {
    private static final Map<String, MethodInfo> getMappings = new HashMap<>();
    private static final Map<String, MethodInfo> postMappings = new HashMap<>();
    private static final Map<String, MethodInfo> putMappings = new HashMap<>();
    private static final Map<String, MethodInfo> deleteMappings = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        scanAndRegisterMappings();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.debug("处理GET请求: {}", req.getRequestURI());
        handleRequest(req, resp, getMappings);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.debug("处理POST请求: {}", req.getRequestURI());
        handleRequest(req, resp, postMappings);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.debug("处理PUT请求: {}", req.getRequestURI());
        handleRequest(req, resp, putMappings);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.debug("处理DELETE请求: {}", req.getRequestURI());
        handleRequest(req, resp, deleteMappings);
    }

    @SuppressWarnings("unchecked")
    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, Map<String, MethodInfo> mappings) throws IOException {
        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            path = path.substring(contextPath.length());
        }
        
        log.debug("处理请求路径: {}", path);
        
        MethodInfo methodInfo = mappings.get(path);
        if (methodInfo == null) {
            methodInfo = findMethodWithPathVariables(path, mappings);
            if (methodInfo != null) {
                log.debug("找到带路径变量的处理方法: {}.{}", 
                        methodInfo.controllerClass.getSimpleName(), 
                        methodInfo.method.getName());
            }
        } else {
            log.debug("找到精确匹配的处理方法: {}.{}", 
                    methodInfo.controllerClass.getSimpleName(), 
                    methodInfo.method.getName());
        }
    
        if (methodInfo != null) {
            try {
                long startTime = System.currentTimeMillis();
                
                Object controller = site.arookieofc.processor.ioc.ApplicationContextHolder.getBean(methodInfo.controllerClass);
                resp.setHeader("Access-Control-Allow-Origin", "*");
                resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
                if (methodInfo.produces.length > 0) {
                    resp.setContentType(methodInfo.produces[0]);
                    if (methodInfo.produces[0].equals("text/event-stream")) {
                        resp.setHeader("Cache-Control", "no-cache");
                        resp.setHeader("Connection", "keep-alive");
                        resp.setHeader("Access-Control-Allow-Origin", "*");
                        resp.setHeader("Access-Control-Allow-Headers", "Cache-Control");
                    }
                }
    
                log.debug("构建方法参数: {}.{}", methodInfo.controllerClass.getSimpleName(), methodInfo.method.getName());
                Object[] args = buildMethodArguments(methodInfo.method, req, resp, path);
                
                log.debug("调用控制器方法: {}.{}", methodInfo.controllerClass.getSimpleName(), methodInfo.method.getName());
                Object result = methodInfo.method.invoke(controller, args);
                
                long executionTime = System.currentTimeMillis() - startTime;
                log.debug("控制器方法执行完成: {}.{}, 耗时: {}ms", 
                        methodInfo.controllerClass.getSimpleName(), 
                        methodInfo.method.getName(), 
                        executionTime);
    
                // 处理流式响应
                if ((methodInfo.produces.length > 0 && 
                (methodInfo.produces[0].equals("text/event-stream") || 
                 methodInfo.produces[0].startsWith("text/plain")))) {
                    // 处理流式响应 - 优化缓冲
                    if (result instanceof Flux) {
                        log.debug("处理Flux流式响应");
                        handleFluxResponse(resp, (Flux<String>) result);
                        return;
                    }
                }
    
                if (methodInfo.produces.length == 0 || methodInfo.produces[0].startsWith("application/json")) {
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.setCharacterEncoding("UTF-8");
                }
                
                if (result != null) {
                    String jsonResponse;
                    if (result instanceof String resultStr) {
                        if (isValidJson(resultStr)) {
                            jsonResponse = resultStr;
                        } else {
                            jsonResponse = objectMapper.writeValueAsString(resultStr);
                        }
                    } else {
                        jsonResponse = objectMapper.writeValueAsString(result);
                    }
                    log.debug("写入响应: 长度={}", jsonResponse.length());
                    resp.getWriter().write(jsonResponse);
                } else {
                    log.debug("写入空响应");
                    resp.getWriter().write("{}");
                }
            } catch (Exception e) {
                log.error("请求处理异常: {}, 路径: {}, 异常: {}", 
                        e.getClass().getSimpleName(), path, e.getMessage());
                boolean handled = GlobalExceptionHandler.handleException(e, req, resp);
                if (!handled) {
                    log.error("全局异常处理器未能处理异常", e);
                    throw new RuntimeException("Request processing failed", e);
                } else {
                    log.debug("异常已被全局异常处理器处理");
                }
            }
        } else {
            log.warn("未找到匹配的处理方法: {}", path);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json;charset=UTF-8");
            try {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "404 Not Found");
                errorResponse.put("path", path);
                resp.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            } catch (Exception e) {
                log.error("写入404响应失败", e);
                resp.getWriter().write("{\"error\":\"404 Not Found\"}");
            }
        }
    }
    
    private void handleFluxResponse(HttpServletResponse resp, Flux<String> flux) {
        try {
            // 强制禁用所有缓冲
            resp.setContentType("text/plain;charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            resp.setBufferSize(1);
            resp.flushBuffer();
            
            PrintWriter writer = resp.getWriter();
            
            try {
                java.util.concurrent.CompletableFuture<Void> future = new java.util.concurrent.CompletableFuture<>();
                writer.write("");
                writer.flush();
                flux.subscribe(
                    data -> {
                        try {
                            writer.write(data);
                            writer.flush();
                            log.trace("流式写入数据: 长度={}", data.length());
                        } catch (Exception e) {
                            log.error("写入token失败: ", e);
                            future.completeExceptionally(e);
                        }
                    },
                    error -> {
                        log.error("流式处理发生错误: ", error);
                        try {
                            writer.write("\n[错误: " + error.getMessage() + "]");
                            writer.flush();
                        } catch (Exception e) {
                            log.error("写入错误信息失败: ", e);
                        }
                        future.completeExceptionally(error);
                    },
                    () -> {
                        log.debug("token流结束");
                        future.complete(null);
                    }
                );
                future.get(60, java.util.concurrent.TimeUnit.SECONDS);
                
            } catch (Exception e) {
                log.error("处理流式响应时发生异常: ", e);
                try {
                    writer.write("\n[服务器错误]");
                    writer.flush();
                } catch (Exception ex) {
                    log.error("写入异常信息失败: ", ex);
                }
            } finally {
                try {
                    writer.close();
                } catch (Exception e) {
                    log.debug("关闭Writer时发生异常: ", e);
                }
            }
        } catch (Exception e) {
            log.error("处理Flux响应失败", e);
        }
    }

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
            registerRequestMapping(method, controllerClass, basePath);
        }
    }

    private static void registerRequestMapping(Method method, Class<?> controllerClass, String basePath) {
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            String path = annotation.value();
            RequestMethod requestMethod = annotation.method();
            String[] produces = annotation.produces();
            String[] consumes = annotation.consumes();
            
            String fullPath = basePath.isEmpty() ? path : basePath + path;
            if (!fullPath.startsWith("/")) {
                fullPath = "/" + fullPath;
            }
            
            MethodInfo methodInfo = new MethodInfo(method, controllerClass, produces, consumes);
            
            switch (requestMethod) {
                case GET:
                    getMappings.put(fullPath, methodInfo);
                    break;
                case POST:
                    postMappings.put(fullPath, methodInfo);
                    break;
                case PUT:
                    putMappings.put(fullPath, methodInfo);
                    break;
                case DELETE:
                    deleteMappings.put(fullPath, methodInfo);
                    break;
            }
        }
    }

    // MethodInfo类定义
    @AllArgsConstructor
    private static class MethodInfo {
        final Method method;
        final Class<?> controllerClass;
        final String[] produces;
        final String[] consumes;
    }

    private static void registerMethodMapping(Method method, Class<?> controllerClass, String basePath,
                                            Class<? extends java.lang.annotation.Annotation> annotationClass,
                                            Map<String, MethodInfo> mappings) {
        if (method.isAnnotationPresent(annotationClass)) {
            try {
                java.lang.annotation.Annotation annotation = method.getAnnotation(annotationClass);
                Method valueMethod = annotationClass.getMethod("value");
                String path = (String) valueMethod.invoke(annotation);

                String[] produces = {};
                String[] consumes = {};
                try {
                    Method producesMethod = annotationClass.getMethod("produces");
                    produces = (String[]) producesMethod.invoke(annotation);
                    Method consumesMethod = annotationClass.getMethod("consumes");
                    consumes = (String[]) consumesMethod.invoke(annotation);
                } catch (NoSuchMethodException e) {
                    // 如果注解没有produces/consumes方法，使用默认值
                }
                
                String fullPath = basePath.isEmpty() ? path : basePath + path;
                if (!fullPath.startsWith("/")) {
                    fullPath = "/" + fullPath;
                }
                
                mappings.put(fullPath, new MethodInfo(method, controllerClass, produces, consumes));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private Object[] buildMethodArguments(Method method, HttpServletRequest req, HttpServletResponse resp, String requestPath) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();

            
            // 检查是否是HttpServletResponse参数
            if (paramType == HttpServletResponse.class) {
                args[i] = resp;
                continue;
            }
            
            // 检查是否是HttpServletRequest参数
            if (paramType == HttpServletRequest.class) {
                args[i] = req;
                continue;
            }
            
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

            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String variableValue = extractPathVariable(requestPath, pathVariable.value());
                args[i] = convertStringToType(variableValue, paramType);
                continue;
            }

            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                String jsonBody = getRequestBody(req);
                args[i] = objectMapper.readValue(jsonBody, paramType);
                continue;
            }
            
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
        String[] pathParts = requestPath.split("/");
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