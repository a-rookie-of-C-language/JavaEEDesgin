package site.arookieofc.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.processor.validation.ValidationProcessor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 提供JWT令牌的生成、验证和解析功能
 */
@Slf4j
public class JWTUtil {
    
    @Config(value = "jwt.secret", defaultValue = "mySecretKey123456")
    private static String SECRET;
    
    @Config(value = "jwt.expiration", defaultValue = "86400000")
    private static long EXPIRATION; // 默认24小时
    
    @Config(value = "jwt.issuer", defaultValue = "JavaEEDesign")
    private static String ISSUER;
    
    private static Algorithm algorithm;
    private static JWTVerifier verifier;
    
    static {
        initializeJWT();
    }
    
    /**
     * 初始化JWT配置
     */
    private static void initializeJWT() {
        try {
            if (SECRET == null || SECRET.isEmpty()) {
                SECRET = "mySecretKey123456";
                log.warn("JWT密钥未配置，使用默认密钥");
            }
            if (EXPIRATION <= 0) {
                EXPIRATION = 86400000L; // 24小时
            }
            if (ISSUER == null || ISSUER.isEmpty()) {
                ISSUER = "JavaEEDesign";
            }
            
            algorithm = Algorithm.HMAC256(SECRET);
            verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build();
            
            log.info("JWT工具类初始化完成，过期时间: {}ms", EXPIRATION);
        } catch (Exception e) {
            log.error("JWT初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("JWT初始化失败", e);
        }
    }
    
    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param username 用户名
     * @param roles 用户角色
     * @return JWT令牌
     */
    public static String generateToken(String userId, String username, String... roles) {
        try {
            ValidationProcessor.validateNotEmpty(userId, "userId", "用户ID不能为空");
            ValidationProcessor.validateNotEmpty(username, "username", "用户名不能为空");
            if (roles == null || roles.length == 0) {
                roles = new String[]{"USER"}; // 默认角色
            }
            
            Date now = new Date();
            Date expireDate = new Date(now.getTime() + EXPIRATION);
            
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(userId)
                    .withClaim("username", username)
                    .withClaim("roles", String.join(",", roles))
                    .withIssuedAt(now)
                    .withExpiresAt(expireDate)
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            log.error("JWT令牌生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("JWT令牌生成失败", e);
        }
    }
    
    /**
     * 生成简单的JWT令牌（只包含用户ID和用户名）
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT令牌
     */
    public static String generateToken(String userId, String username) {
        return generateToken(userId, username, "USER");
    }
    
    /**
     * 验证JWT令牌
     * @param token JWT令牌
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            try {
                ValidationProcessor.validateNotEmpty(token, "token", "令牌不能为空");
            } catch (IllegalArgumentException e) {
                log.debug("令牌验证失败: {}", e.getMessage());
                return false;
            }
            
            // 移除Bearer前缀（如果存在）
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.debug("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 解析JWT令牌
     * @param token JWT令牌
     * @return 解析后的JWT对象
     */
    public static DecodedJWT parseToken(String token) {
        try {
            ValidationProcessor.validateNotEmpty(token, "token", "令牌不能为空");
            
            // 移除Bearer前缀（如果存在）
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error("JWT令牌解析失败: {}", e.getMessage());
            throw new RuntimeException("JWT令牌解析失败", e);
        }
    }
    
    /**
     * 从令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public static String getUserId(String token) {
        try {
            ValidationProcessor.validateNotEmpty(token, "token", "令牌不能为空");
        } catch (IllegalArgumentException e) {
            log.debug("获取用户ID失败: {}", e.getMessage());
            return null;
        }
        DecodedJWT decodedJWT = parseToken(token);
        return decodedJWT.getSubject();
    }
    
    /**
     * 从令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public static String getUsername(String token) {
        try {
            ValidationProcessor.validateNotEmpty(token, "token", "令牌不能为空");
        } catch (IllegalArgumentException e) {
            log.debug("获取用户名失败: {}", e.getMessage());
            return null;
        }
        DecodedJWT decodedJWT = parseToken(token);
        return decodedJWT.getClaim("username").asString();
    }
    
    /**
     * 从令牌中获取用户角色
     * @param token JWT令牌
     * @return 用户角色数组
     */
    public static String[] getRoles(String token) {
        try {
            ValidationProcessor.validateNotEmpty(token, "token", "令牌不能为空");
        } catch (IllegalArgumentException e) {
            log.debug("获取角色失败: {}", e.getMessage());
            return new String[0];
        }
        DecodedJWT decodedJWT = parseToken(token);
        String rolesStr = decodedJWT.getClaim("roles").asString();
        return rolesStr != null ? rolesStr.split(",") : new String[0];
    }
    
    /**
     * 获取令牌的过期时间
     * @param token JWT令牌
     * @return 过期时间
     */
    public static Date getExpirationDate(String token) {
        DecodedJWT decodedJWT = parseToken(token);
        return decodedJWT.getExpiresAt();
    }
    
    /**
     * 检查令牌是否即将过期（剩余时间少于30分钟）
     * @param token JWT令牌
     * @return 是否即将过期
     */
    public static boolean isTokenExpiringSoon(String token) {
        try {
            ValidationProcessor.validateNotEmpty(token, "token", "令牌不能为空");
        } catch (IllegalArgumentException e) {
            log.debug("检查令牌过期状态失败: {}", e.getMessage());
            return true;
        }
        try {
            Date expirationDate = getExpirationDate(token);
            Date now = new Date();
            long timeLeft = expirationDate.getTime() - now.getTime();
            return timeLeft < 30 * 60 * 1000; // 30分钟
        } catch (Exception e) {
            return true; // 如果无法解析，认为即将过期
        }
    }
    
    /**
     * 获取令牌中的所有声明
     * @param token JWT令牌
     * @return 声明映射
     */
    public static Map<String, Object> getAllClaims(String token) {
        DecodedJWT decodedJWT = parseToken(token);
        Map<String, Object> claims = new HashMap<>();
        
        claims.put("userId", decodedJWT.getSubject());
        claims.put("username", decodedJWT.getClaim("username").asString());
        claims.put("roles", decodedJWT.getClaim("roles").asString());
        claims.put("issuer", decodedJWT.getIssuer());
        claims.put("issuedAt", decodedJWT.getIssuedAt());
        claims.put("expiresAt", decodedJWT.getExpiresAt());
        
        return claims;
    }
    
    /**
     * 刷新令牌（生成新的令牌）
     * @param token 原始令牌
     * @return 新的令牌
     */
    public static String refreshToken(String token) {
        try {
            ValidationProcessor.validateNotEmpty(token, "token", "令牌不能为空");
            DecodedJWT decodedJWT = parseToken(token);
            String userId = decodedJWT.getSubject();
            String username = decodedJWT.getClaim("username").asString();
            String rolesStr = decodedJWT.getClaim("roles").asString();
            String[] roles = rolesStr != null ? rolesStr.split(",") : new String[]{"USER"};
            
            return generateToken(userId, username, roles);
        } catch (Exception e) {
            log.error("令牌刷新失败: {}", e.getMessage(), e);
            throw new RuntimeException("令牌刷新失败", e);
        }
    }
}
