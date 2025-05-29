package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.service.AiService;
import site.arookieofc.service.impl.AiServiceImpl;
import site.arookieofc.pojo.dto.Result;

import java.util.List;
import java.util.Map;

@Controller("/ai")
public class AiController {
    
    private final AiService aiService = new AiServiceImpl();
    
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
