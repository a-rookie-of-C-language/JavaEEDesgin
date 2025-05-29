package site.arookieofc.service;

import java.util.List;
import java.util.Map;

public interface AiService {

    String chat(String message);

    String chatWithHistory(List<Map<String, String>> messages);
    
    /**
     * 带会话ID的聊天，自动保存历史记录
     */
    String chatWithSession(String sessionId, String userId, String message);
    
    /**
     * 获取会话历史记录
     */
    List<Map<String, Object>> getSessionHistory(String sessionId);
    
    /**
     * 获取用户的所有会话
     */
    List<String> getUserSessions(String userId);
    
    /**
     * 删除会话
     */
    boolean deleteSession(String sessionId);
    
    /**
     * 支持Function Call的聊天
     */
    String chatWithFunctionCall(String sessionId, String userId, String message);

    boolean isAvailable();

    Map<String, String> getModelInfo();
}