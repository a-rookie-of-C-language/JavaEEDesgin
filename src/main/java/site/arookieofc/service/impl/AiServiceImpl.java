package site.arookieofc.service.impl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.service.AiService;
import site.arookieofc.utils.ModelUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            
            if (chatMessages.isEmpty()) {
                throw new IllegalArgumentException("没有有效的对话消息");
            }
            
            return model.chat(chatMessages).toString();
        } catch (Exception e) {
            System.err.println("AI多轮对话服务异常: " + e.getMessage());
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            ChatLanguageModel model = ModelUtil.getLanguageModel();
            // 发送一个简单的测试消息
            String response = model.chat("hello");
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            System.err.println("AI服务可用性检查失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Map<String, String> getModelInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("url", aiUrl != null ? aiUrl : "未配置");
        info.put("modelName", modelName != null ? modelName : "未配置");
        info.put("status", isAvailable() ? "可用" : "不可用");
        return info;
    }
}