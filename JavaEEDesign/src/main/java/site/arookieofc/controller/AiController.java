package site.arookieofc.controller;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.web.Controller;
import site.arookieofc.annotation.web.PostMapping;
import site.arookieofc.annotation.web.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import dev.langchain4j.service.TokenStream;
import site.arookieofc.service.AiService;

import java.util.Map;

@Slf4j
@Controller("/ai")
@Component
public class AiController {
    
    @Autowired
    private AiService aiService;  // 改为注入基础AI服务

    @PostMapping(value = "/chat-stream", produces = "text/plain")
    public Flux<String> chatStream(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("收到聊天请求: {}", message);
        
        // 将TokenStream转换为Flux<String>
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        TokenStream tokenStream = aiService.chatStream(message);
        
        tokenStream
            .onPartialResponse(sink::tryEmitNext)
            .onCompleteResponse((response)-> sink.tryEmitComplete())
            .onError(sink::tryEmitError)
            .start();
        
        log.info("返回流式响应");
        return sink.asFlux();
    }
}
