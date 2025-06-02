package site.arookieofc.service.impl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.service.AiService;
import site.arookieofc.utils.ModelUtil;
import java.util.*;

@Slf4j
@Component
public class AiServiceImpl implements AiService {
    
    @Config("ai.url")
    private static String aiUrl;
    
    @Config("ai.name")
    private static String modelName;

    @Override
    public String chat(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        
        try {
            ChatLanguageModel model = ModelUtil.getLanguageModel();
            return model.chat(message.trim());
        } catch (Exception e) {
            e.fillInStackTrace();
            System.err.println("AI聊天服务异常: " + e.getMessage());
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }
    
    @Override
    public String chatWithHistory(List<Map<String, String>> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("对话历史不能为空");
        }
        
        try {
            ChatLanguageModel model = ModelUtil.getLanguageModel();
            List<ChatMessage> chatMessages = convertToChatMessages(messages);
            
            if (chatMessages.isEmpty()) {
                throw new IllegalArgumentException("没有有效的对话消息");
            }
            
            return model.chat(chatMessages).toString();
        } catch (Exception e) {
            e.fillInStackTrace();
            System.err.println("AI多轮对话服务异常: " + e.getMessage());
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    @Override
    public Flux<String> chatStream(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        try {
            StreamingChatLanguageModel model = ModelUtil.getStreamingLanguageModel();
            return Flux.create(sink -> model.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    // 按照token输出
                    sink.next(partialResponse);
                }
                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    sink.complete();
                }
                @Override
                public void onError(Throwable error) {
                    sink.error(error);
                }
            }));
        } catch (Exception e) {
            e.fillInStackTrace();
            System.err.println("AI流式聊天服务异常: " + e.getMessage());
            return null;
        }
    }

    private List<ChatMessage> convertToChatMessages(List<Map<String, String>> messages) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        
        for (Map<String, String> msg : messages) {
            String role = msg.get("role");
            String content = msg.get("content");
            
            if (content == null || content.trim().isEmpty()) {
                continue;
            }
            
            if ("user".equals(role)) {
                chatMessages.add(UserMessage.from(content));
            } else if ("assistant".equals(role) || "ai".equals(role)) {
                chatMessages.add(AiMessage.from(content));
            }
        }
        
        return chatMessages;
    }
    

    
    @Override
    public boolean isAvailable() {
        try {
            ChatLanguageModel model = ModelUtil.getLanguageModel();
            String response = model.chat("hello");
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            e.fillInStackTrace();
            System.err.println("AI服务可用性检查失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Map<String, String> getModelInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", modelName != null ? modelName : "未知");
        info.put("url", aiUrl != null ? aiUrl : "未知");
        info.put("version", "1.0");
        return info;
    }
}