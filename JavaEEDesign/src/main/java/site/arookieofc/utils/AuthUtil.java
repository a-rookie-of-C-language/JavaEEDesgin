package site.arookieofc.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.processor.validation.ValidationProcessor;

import java.util.Arrays;
import java.util.List;

/**
 * 认证工具类
 * 提供便捷的方法来获取当前请求的用户信息
 */
@Slf4j
public class AuthUtil {
    
    /**
     * 从请求中获取当前用户ID
     * @param request HTTP请求
     * @return 用户ID，如果未认证则返回null
     */
    public static String getCurrentUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (String) request.getAttribute("userId");
    }
    
    /**
     * 从请求中获取当前用户名
     * @param request HTTP请求
     * @return 用户名，如果未认证则返回null
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (String) request.getAttribute("username");
    }
    
    /**
     * 从请求中获取当前用户角色
     * @param request HTTP请求
     * @return 用户角色数组，如果未认证则返回空数组
     */
    public static String[] getCurrentUserRoles(HttpServletRequest request) {
        if (request == null) {
            return new String[0];
        }
        String[] roles = (String[]) request.getAttribute("roles");
        return roles != null ? roles : new String[0];
    }
    
    /**
     * 从请求中获取当前JWT令牌
     * @param request HTTP请求
     * @return JWT令牌，如果未认证则返回null
     */
    public static String getCurrentToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (String) request.getAttribute("token");
    }
    
    /**
     * 检查当前用户是否已认证
     * @param request HTTP请求
     * @return 是否已认证
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUserId(request) != null;
    }
    
    /**
     * 检查当前用户是否具有指定角色
     * @param request HTTP请求
     * @param role 角色名称
     * @return 是否具有指定角色
     */
    public static boolean hasRole(HttpServletRequest request, String role) {
        try {
            ValidationProcessor.validateNotEmpty(role, "role", "角色名称不能为空");
        } catch (IllegalArgumentException e) {
            log.debug("角色验证失败: {}", e.getMessage());
            return false;
        }
        
        String[] roles = getCurrentUserRoles(request);
        return Arrays.asList(roles).contains(role);
    }
    
    /**
     * 检查当前用户是否具有任意一个指定角色
     * @param request HTTP请求
     * @param roles 角色名称列表
     * @return 是否具有任意一个指定角色
     */
    public static boolean hasAnyRole(HttpServletRequest request, String... roles) {
        if (roles == null || roles.length == 0) {
            log.debug("角色列表为空或null");
            return false;
        }
        
        String[] userRoles = getCurrentUserRoles(request);
        List<String> userRoleList = Arrays.asList(userRoles);
        
        for (String role : roles) {
            try {
                ValidationProcessor.validateNotEmpty(role, "role", "角色名称不能为空");
                if (userRoleList.contains(role)) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                log.debug("跳过无效角色: {}", e.getMessage());
                continue;
            }
        }
        
        return false;
    }
    
    /**
     * 检查当前用户是否具有所有指定角色
     * @param request HTTP请求
     * @param roles 角色名称列表
     * @return 是否具有所有指定角色
     */
    public static boolean hasAllRoles(HttpServletRequest request, String... roles) {
        if (roles == null || roles.length == 0) {
            return true;
        }
        
        String[] userRoles = getCurrentUserRoles(request);
        List<String> userRoleList = Arrays.asList(userRoles);
        
        for (String role : roles) {
            try {
                ValidationProcessor.validateNotEmpty(role, "role", "角色名称不能为空");
                if (!userRoleList.contains(role)) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                log.debug("角色验证失败，视为不具备该角色: {}", e.getMessage());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查当前用户是否为管理员
     * @param request HTTP请求
     * @return 是否为管理员
     */
    public static boolean isAdmin(HttpServletRequest request) {
        return hasAnyRole(request, "ADMIN", "ADMINISTRATOR", "ROOT");
    }
    
    /**
     * 检查当前用户是否为普通用户
     * @param request HTTP请求
     * @return 是否为普通用户
     */
    public static boolean isUser(HttpServletRequest request) {
        return hasRole(request, "USER");
    }
    
    /**
     * 获取用户信息摘要
     * @param request HTTP请求
     * @return 用户信息字符串
     */
    public static String getUserInfo(HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return "未认证用户";
        }
        
        String userId = getCurrentUserId(request);
        String username = getCurrentUsername(request);
        String[] roles = getCurrentUserRoles(request);
        
        return String.format("用户[ID: %s, 用户名: %s, 角色: %s]", 
                userId, username, Arrays.toString(roles));
    }
    
    /**
     * 记录用户操作日志
     * @param request HTTP请求
     * @param action 操作描述
     */
    public static void logUserAction(HttpServletRequest request, String action) {
        try {
            ValidationProcessor.validateNotEmpty(action, "action", "操作描述不能为空");
            if (isAuthenticated(request)) {
                String userInfo = getUserInfo(request);
                log.info("用户操作: {} - {}", userInfo, action);
            } else {
                log.info("匿名用户操作: {}", action);
            }
        } catch (IllegalArgumentException e) {
            log.warn("记录用户操作失败: {}", e.getMessage());
        }
    }
    
    /**
     * 验证用户权限
     * @param request HTTP请求
     * @param requiredRole 需要的角色
     * @param resourceId 资源ID（可选）
     * @return 是否有权限
     */
    public static boolean checkPermission(HttpServletRequest request, String requiredRole, String resourceId) {
        try {
            ValidationProcessor.validateNotEmpty(requiredRole, "requiredRole", "需要的角色不能为空");
        } catch (IllegalArgumentException e) {
            log.warn("权限检查失败: {}", e.getMessage());
            return false;
        }
        
        if (!isAuthenticated(request)) {
            log.warn("未认证用户尝试访问需要角色 {} 的资源: {}", requiredRole, resourceId);
            return false;
        }
        
        if (!hasRole(request, requiredRole)) {
            String userInfo = getUserInfo(request);
            log.warn("用户 {} 尝试访问需要角色 {} 的资源: {}", userInfo, requiredRole, resourceId);
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证用户是否可以访问指定用户的资源（用户只能访问自己的资源，管理员可以访问所有资源）
     * @param request HTTP请求
     * @param targetUserId 目标用户ID
     * @return 是否有权限
     */
    public static boolean canAccessUserResource(HttpServletRequest request, String targetUserId) {
        try {
            ValidationProcessor.validateNotEmpty(targetUserId, "targetUserId", "目标用户ID不能为空");
        } catch (IllegalArgumentException e) {
            log.warn("用户资源访问检查失败: {}", e.getMessage());
            return false;
        }
        
        if (!isAuthenticated(request)) {
            return false;
        }
        
        // 管理员可以访问所有用户资源
        if (isAdmin(request)) {
            return true;
        }
        
        // 用户只能访问自己的资源
        String currentUserId = getCurrentUserId(request);
        try {
            ValidationProcessor.validateNotEmpty(currentUserId, "currentUserId", "当前用户ID不能为空");
            return currentUserId.equals(targetUserId);
        } catch (IllegalArgumentException e) {
            log.debug("当前用户ID为空: {}", e.getMessage());
            return false;
        }
    }
}