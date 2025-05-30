package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.service.AiService;
import site.arookieofc.pojo.dto.Result;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller("/ai")
@Component
public class AiController {
    
    @Autowired
    private AiService aiService;
    
    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            String aiResponse = aiService.chat(message);
            return Result.success("对话成功", Map.of("response", aiResponse));
        } catch (Exception e) {
            return Result.error("AI对话失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/chat-session")
    public Result chatWithSession(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String userId = request.get("userId");
            String message = request.get("message");
            
            if (message == null || message.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            // 如果没有sessionId，生成一个新的
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }
            
            String aiResponse = aiService.chatWithSession(sessionId, userId, message);
            return Result.success("会话对话成功", Map.of(
                "response", aiResponse,
                "sessionId", sessionId
            ));
        } catch (Exception e) {
            return Result.error("AI会话对话失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/chat-function")
    public Result chatWithFunction(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String userId = request.get("userId");
            String message = request.get("message");
            
            if (message == null || message.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            // 如果没有sessionId，生成一个新的
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }
            
            String aiResponse = aiService.chatWithFunctionCall(sessionId, userId, message);
            return Result.success("函数调用对话成功", Map.of(
                "response", aiResponse,
                "sessionId", sessionId
            ));
        } catch (Exception e) {
            return Result.error("AI函数调用对话失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/history/{sessionId}")
    public Result getSessionHistory(@PathVariable String sessionId) {
        try {
            List<Map<String, Object>> history = aiService.getSessionHistory(sessionId);
            return Result.success("获取历史记录成功", Map.of("history", history));
        } catch (Exception e) {
            return Result.error("获取历史记录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/sessions/{userId}")
    public Result getUserSessions(@PathVariable String userId) {
        try {
            List<String> sessions = aiService.getUserSessions(userId);
            return Result.success("获取用户会话成功", Map.of("sessions", sessions));
        } catch (Exception e) {
            return Result.error("获取用户会话失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/session/{sessionId}")
    public Result deleteSession(@PathVariable String sessionId) {
        try {
            boolean success = aiService.deleteSession(sessionId);
            if (success) {
                return Result.success("删除会话成功", null);
            } else {
                return Result.error("删除会话失败");
            }
        } catch (Exception e) {
            return Result.error("删除会话失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/chat-history")
    public Result chatWithHistory(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) request.get("messages");
            
            if (messages == null || messages.isEmpty()) {
                return Result.error("对话历史不能为空");
            }
            
            String aiResponse = aiService.chatWithHistory(messages);
            return Result.success("多轮对话成功", Map.of("response", aiResponse));
        } catch (Exception e) {
            return Result.error("AI多轮对话失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    public Result getStatus() {
        try {
            boolean available = aiService.isAvailable();
            Map<String, String> modelInfo = aiService.getModelInfo();
            
            return Result.success("状态检查完成", Map.of(
                "available", available,
                "modelInfo", modelInfo
            ));
        } catch (Exception e) {
            return Result.error("状态检查失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public Result health() {
        try {
            return Result.success("AI服务正常运行", Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", "healthy"
            ));
        } catch (Exception e) {
            return Result.error("健康检查失败: " + e.getMessage());
        }
    }
}
