package site.arookieofc.controller;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.web.Controller;
import site.arookieofc.annotation.web.PostMapping;
import site.arookieofc.annotation.web.RequestBody;
import reactor.core.publisher.Flux;
import site.arookieofc.service.AiService;

import java.util.Map;

@Slf4j
@Controller("/ai")
public class AiController {
    
    @Autowired
    private AiService aiService;
    
    @PostMapping("/chat-stream")
    public Flux<String> chatStream(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("收到聊天请求: {}", message);
        return aiService.chatStream(message);
    }
    
    @PostMapping("/initialize")
    public String initializeMcp() {
        try {
            // 由于使用了@PostConstruct，MCP服务器会自动初始化
            // 这里可以添加额外的初始化逻辑或状态检查
            return "MCP服务器已自动初始化";
        } catch (Exception e) {
            log.error("MCP初始化检查失败", e);
            return "MCP服务器初始化检查失败: " + e.getMessage();
        }
    }
    
    @PostMapping("/shutdown")
    public String shutdownMcp() {
        try {
            // 由于使用了@PreDestroy，MCP服务器会自动关闭
            // 这里可以添加额外的关闭逻辑或状态检查
            return "MCP服务器将在应用关闭时自动停止";
        } catch (Exception e) {
            log.error("MCP关闭检查失败", e);
            return "MCP服务器关闭检查失败: " + e.getMessage();
        }
    }
}
