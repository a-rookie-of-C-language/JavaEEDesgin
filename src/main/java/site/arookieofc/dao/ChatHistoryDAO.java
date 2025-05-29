package site.arookieofc.dao;

import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.entity.ChatHistory;

import java.util.List;
import java.util.Optional;

public interface ChatHistoryDAO {
    
    @SQL(value = "INSERT INTO chat_history (sessionId, userId, role, content, functionCall, functionResult, createTime, updateTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", type = "INSERT")
    int addChatHistory(String sessionId, String userId, String role, String content, String functionCall, String functionResult, java.time.LocalDateTime createTime, java.time.LocalDateTime updateTime);
    
    @SQL("SELECT * FROM chat_history WHERE sessionId = ? ORDER BY createTime ASC")
    List<ChatHistory> getChatHistoryBySessionId(String sessionId);
    
    @SQL("SELECT * FROM chat_history WHERE sessionId = ? ORDER BY createTime ASC LIMIT ? OFFSET ?")
    List<ChatHistory> getChatHistoryBySessionIdWithPaging(String sessionId, int limit, int offset);
    
    @SQL("SELECT * FROM chat_history WHERE userId = ? ORDER BY createTime DESC")
    List<ChatHistory> getChatHistoryByUserId(String userId);
    
    @SQL("SELECT DISTINCT sessionId FROM chat_history WHERE userId = ? ORDER BY MAX(createTime) DESC")
    List<String> getSessionsByUserId(String userId);
    
    @SQL("SELECT COUNT(*) FROM chat_history WHERE sessionId = ?")
    int getMessageCountBySessionId(String sessionId);
    
    @SQL(value = "DELETE FROM chat_history WHERE sessionId = ?", type = "DELETE")
    boolean deleteChatHistoryBySessionId(String sessionId);
    
    @SQL(value = "DELETE FROM chat_history WHERE createTime < ?", type = "DELETE")
    boolean deleteOldChatHistory(java.time.LocalDateTime beforeTime);
}