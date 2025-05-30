package site.arookieofc.service;



import java.util.List;
import java.util.Map;


public interface AiService {

    String chat(String message);

    String chatWithHistory(List<Map<String, String>> messages);

    String chatWithSession(String sessionId, String userId, String message);

    List<Map<String, Object>> getSessionHistory(String sessionId);

    List<String> getUserSessions(String userId);

    boolean deleteSession(String sessionId);

    String chatWithFunctionCall(String sessionId, String userId, String message);

    boolean isAvailable();

    Map<String, String> getModelInfo();
}