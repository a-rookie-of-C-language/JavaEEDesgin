package site.arookieofc.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.utils.JWTUtil;
import site.arookieofc.processor.validation.ValidationProcessor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class JWTFilter implements Filter {
    
    @Config(value = "jwt.filter.enabled", defaultValue = "true")
    private static boolean FILTER_ENABLED;
    
    @Config(value = "jwt.filter.exclude-paths", defaultValue = "/login,/register,/public,/static,/css,/js,/images")
    private static String EXCLUDE_PATHS;
    
    private Set<String> excludePaths;
    
    @Override
    public void init(FilterConfig filterConfig) {
        log.info("JWT过滤器初始化开始");
        
        // 初始化排除路径
        excludePaths = new HashSet<>();
        try {
            ValidationProcessor.validateNotEmpty(EXCLUDE_PATHS, "EXCLUDE_PATHS", "排除路径配置不能为空");
            String[] paths = EXCLUDE_PATHS.split(",");
            for (String path : paths) {
                excludePaths.add(path.trim());
            }
        } catch (IllegalArgumentException e) {
            log.warn("排除路径配置为空，使用默认配置: {}", e.getMessage());
        }
        
        // 添加默认排除路径
        excludePaths.add("/login");
        excludePaths.add("/register");
        excludePaths.add("/public");
        excludePaths.add("/static");
        excludePaths.add("/css");
        excludePaths.add("/js");
        excludePaths.add("/images");
        excludePaths.add("/favicon.ico");
        
        log.info("JWT过滤器初始化完成，排除路径: {}", excludePaths);
        log.info("JWT过滤器状态: {}", FILTER_ENABLED ? "启用" : "禁用");
    }
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        // 设置响应头
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        // 如果过滤器被禁用，直接放行
        if (!FILTER_ENABLED) {
            log.debug("JWT过滤器已禁用，直接放行");
            filterChain.doFilter(request, response);
            return;
        }
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("JWT过滤器处理请求: {} {}", method, requestURI);
        
        // 检查是否为排除路径
        if (isExcludePath(requestURI)) {
            log.debug("请求路径 {} 在排除列表中，直接放行", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 处理OPTIONS请求（CORS预检请求）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            handleCorsRequest(response);
            return;
        }
        
        // 获取JWT令牌
        String token = extractToken(request);
        
        try {
            ValidationProcessor.validateNotEmpty(token, "token", "JWT令牌不能为空");
        } catch (IllegalArgumentException e) {
            log.warn("请求 {} 缺少JWT令牌: {}", requestURI, e.getMessage());
            sendUnauthorizedResponse(response, "缺少认证令牌");
            return;
        }
        
        // 验证JWT令牌
        if (!JWTUtil.validateToken(token)) {
            log.warn("请求 {} 的JWT令牌无效", requestURI);
            sendUnauthorizedResponse(response, "无效的认证令牌");
            return;
        }
        
        try {
            // 获取用户信息并设置到请求属性中
            String userId = JWTUtil.getUserId(token);
            String username = JWTUtil.getUsername(token);
            String[] roles = JWTUtil.getRoles(token);
            
            // 将用户信息设置到请求属性中，供后续处理使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("roles", roles);
            request.setAttribute("token", token);
            
            log.debug("用户 {} (ID: {}) 通过JWT验证，角色: {}", username, userId, Arrays.toString(roles));
            
            // 检查令牌是否即将过期，如果是则在响应头中添加刷新提示
            if (JWTUtil.isTokenExpiringSoon(token)) {
                response.setHeader("X-Token-Refresh", "true");
                log.debug("用户 {} 的令牌即将过期，建议刷新", username);
            }
            
            // 继续处理请求
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("JWT令牌处理异常: {}", e.getMessage(), e);
            sendUnauthorizedResponse(response, "令牌处理异常");
        }
    }
    
    /**
     * 检查请求路径是否在排除列表中
     * @param requestURI 请求URI
     * @return 是否排除
     */
    private boolean isExcludePath(String requestURI) {
        try {
            ValidationProcessor.validateNotEmpty(requestURI, "requestURI", "请求URI不能为空");
        } catch (IllegalArgumentException e) {
            log.debug("请求URI为空: {}", e.getMessage());
            return false;
        }
        
        // 精确匹配
        if (excludePaths.contains(requestURI)) {
            return true;
        }
        
        // 前缀匹配
        for (String excludePath : excludePaths) {
            if (requestURI.startsWith(excludePath)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 从请求中提取JWT令牌
     * @param request HTTP请求
     * @return JWT令牌
     */
    private String extractToken(HttpServletRequest request) {
        // 1. 从Authorization头中获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 2. 从请求参数中获取
        String tokenParam = request.getParameter("token");
        try {
            ValidationProcessor.validateNotEmpty(tokenParam, "tokenParam", "请求参数token不能为空");
            return tokenParam;
        } catch (IllegalArgumentException e) {
            // 继续尝试其他方式
        }
        
        // 3. 从自定义头中获取
        String customHeader = request.getHeader("X-Auth-Token");
        try {
            ValidationProcessor.validateNotEmpty(customHeader, "customHeader", "自定义头X-Auth-Token不能为空");
            return customHeader;
        } catch (IllegalArgumentException e) {
            // 所有方式都失败
        }
        
        return null;
    }
    
    /**
     * 处理CORS预检请求
     * @param response HTTP响应
     */
    private void handleCorsRequest(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-Token");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * 发送未授权响应
     * @param response HTTP响应
     * @param message 错误消息
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String jsonResponse = String.format(
            "{\"success\": false, \"code\": 401, \"message\": \"%s\", \"timestamp\": %d}",
            message, System.currentTimeMillis()
        );
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonResponse);
            writer.flush();
        }
    }
    
    @Override
    public void destroy() {
        log.info("JWT过滤器销毁");
        if (excludePaths != null) {
            excludePaths.clear();
        }
    }
}
