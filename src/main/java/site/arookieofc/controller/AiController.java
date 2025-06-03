package site.arookieofc.controller;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import site.arookieofc.annotation.web.*;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.service.AiService;
import java.util.Map;


@Slf4j
@Controller("/ai")
@Component
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping(value = "/chat-stream", produces = "text/plain;charset=UTF-8")
    public Flux<String> chatStream(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Flux.error(new IllegalArgumentException("消息内容不能为空"));
        }
        return aiService.chatStream(message);
    }
}
