package site.arookieofc.controller;

import reactor.core.publisher.Flux;
import site.arookieofc.annotation.web.*;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.service.AiService;
import site.arookieofc.pojo.dto.Result;

import java.util.List;
import java.util.Map;


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

    @PostMapping(value = "/chat-stream", produces = "text/plain;charset=UTF-8")
    public Flux<String> chatStream(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return Flux.error(new IllegalArgumentException("消息内容不能为空"));
            }
            return aiService.chatStream(message);
        } catch (Exception e) {
            e.fillInStackTrace();
            return Flux.error(e);
        }
    }
}
