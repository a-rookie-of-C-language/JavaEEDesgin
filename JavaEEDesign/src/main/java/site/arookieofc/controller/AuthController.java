package site.arookieofc.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.web.Controller;
import site.arookieofc.annotation.web.PostMapping;
import site.arookieofc.annotation.web.RequestBody;
import site.arookieofc.annotation.web.GetMapping;
import site.arookieofc.utils.AuthUtil;
import site.arookieofc.utils.JWTUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 认证控制器
 * 处理用户登录、注册、令牌刷新等认证相关操作
 */
@Slf4j
@Controller
public class AuthController {
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @PostMapping("/api/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            // 验证输入参数
            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名不能为空");
                return response;
            }
            
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "密码不能为空");
                return response;
            }
            
            // 这里应该调用用户服务验证用户名和密码
            // 为了演示，我们使用简单的硬编码验证
            boolean isValidUser = validateUser(username, password);
            
            if (!isValidUser) {
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                log.warn("用户 {} 登录失败：用户名或密码错误", username);
                return response;
            }
            
            // 生成JWT令牌
            String userId = getUserId(username); // 获取用户ID
            String[] roles = getUserRoles(username); // 获取用户角色
            String token = JWTUtil.generateToken(userId, username, roles);
            
            // 构建成功响应
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("user", Map.of(
                "id", userId,
                "username", username,
                "roles", roles
            ));
            
            log.info("用户 {} (ID: {}) 登录成功", username, userId);
            
        } catch (Exception e) {
            log.error("登录处理异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "登录处理异常");
        }
        
        return response;
    }
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/api/auth/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = registerRequest.get("username");
            String password = registerRequest.get("password");
            String email = registerRequest.get("email");
            
            // 验证输入参数
            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名不能为空");
                return response;
            }
            
            if (password == null || password.length() < 6) {
                response.put("success", false);
                response.put("message", "密码长度不能少于6位");
                return response;
            }
            
            // 检查用户名是否已存在
            if (isUsernameExists(username)) {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return response;
            }
            
            // 这里应该调用用户服务创建新用户
            // 为了演示，我们假设注册成功
            String userId = createUser(username, password, email);
            
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("userId", userId);
            
            log.info("新用户注册成功: {} (ID: {})", username, userId);
            
        } catch (Exception e) {
            log.error("注册处理异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "注册处理异常");
        }
        
        return response;
    }
    
    /**
     * 刷新令牌
     * @param request HTTP请求
     * @return 刷新结果
     */
    @PostMapping("/api/auth/refresh")
    public Map<String, Object> refreshToken(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentToken = AuthUtil.getCurrentToken(request);
            
            if (currentToken == null) {
                response.put("success", false);
                response.put("message", "缺少认证令牌");
                return response;
            }
            
            // 刷新令牌
            String newToken = JWTUtil.refreshToken(currentToken);
            
            response.put("success", true);
            response.put("message", "令牌刷新成功");
            response.put("token", newToken);
            
            String username = AuthUtil.getCurrentUsername(request);
            log.info("用户 {} 的令牌刷新成功", username);
            
        } catch (Exception e) {
            log.error("令牌刷新异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "令牌刷新失败");
        }
        
        return response;
    }
    
    /**
     * 获取当前用户信息
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/api/auth/me")
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!AuthUtil.isAuthenticated(request)) {
                response.put("success", false);
                response.put("message", "用户未认证");
                return response;
            }
            
            String userId = AuthUtil.getCurrentUserId(request);
            String username = AuthUtil.getCurrentUsername(request);
            String[] roles = AuthUtil.getCurrentUserRoles(request);
            
            response.put("success", true);
            response.put("user", Map.of(
                "id", userId,
                "username", username,
                "roles", roles
            ));
            
        } catch (Exception e) {
            log.error("获取用户信息异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取用户信息失败");
        }
        
        return response;
    }
    
    /**
     * 用户登出
     * @param request HTTP请求
     * @return 登出结果
     */
    @PostMapping("/api/auth/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = AuthUtil.getCurrentUsername(request);
            
            // 这里可以将令牌加入黑名单或执行其他登出逻辑
            // 由于JWT是无状态的，客户端删除令牌即可实现登出
            
            response.put("success", true);
            response.put("message", "登出成功");
            
            if (username != null) {
                log.info("用户 {} 登出成功", username);
            }
            
        } catch (Exception e) {
            log.error("登出处理异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "登出处理异常");
        }
        
        return response;
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 验证用户凭据（示例实现）
     * 实际项目中应该查询数据库并验证密码哈希
     */
    private boolean validateUser(String username, String password) {
        // 示例用户数据
        Map<String, String> users = Map.of(
            "admin", "admin123",
            "user", "user123",
            "test", "test123"
        );
        
        return users.containsKey(username) && users.get(username).equals(password);
    }
    
    /**
     * 获取用户ID（示例实现）
     */
    private String getUserId(String username) {
        // 实际项目中应该从数据库查询
        Map<String, String> userIds = Map.of(
            "admin", "1",
            "user", "2",
            "test", "3"
        );
        
        return userIds.getOrDefault(username, "unknown");
    }
    
    /**
     * 获取用户角色（示例实现）
     */
    private String[] getUserRoles(String username) {
        // 实际项目中应该从数据库查询
        Map<String, String[]> userRoles = Map.of(
            "admin", new String[]{"ADMIN", "USER"},
            "user", new String[]{"USER"},
            "test", new String[]{"USER"}
        );
        
        return userRoles.getOrDefault(username, new String[]{"USER"});
    }
    
    /**
     * 检查用户名是否已存在（示例实现）
     */
    private boolean isUsernameExists(String username) {
        // 实际项目中应该查询数据库
        return Set.of("admin", "user", "test").contains(username);
    }
    
    /**
     * 创建新用户（示例实现）
     */
    private String createUser(String username, String password, String email) {
        // 实际项目中应该保存到数据库
        // 这里只是返回一个模拟的用户ID
        return "user_" + System.currentTimeMillis();
    }
}