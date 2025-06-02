package site.arookieofc.service;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface AiService {

    String chat(String message);

    String chatWithHistory(List<Map<String, String>> messages);
    // 新增流式聊天方法
    Flux<String> chatStream(String message);

    boolean isAvailable();

    Map<String, String> getModelInfo();
}