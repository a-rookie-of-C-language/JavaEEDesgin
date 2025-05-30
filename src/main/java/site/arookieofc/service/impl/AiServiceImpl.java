package site.arookieofc.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.dao.ChatHistoryDAO;
import site.arookieofc.entity.ChatHistory;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.service.AiService;
import site.arookieofc.service.FunctionCallService;
import site.arookieofc.utils.ModelUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
public class AiServiceImpl implements AiService {
    
    @Config("ai.url")
    private static String aiUrl;
    
    @Config("ai.name")
    private static String modelName;
    
    private final ChatHistoryDAO chatHistoryDAO = DAOFactory.getDAO(ChatHistoryDAO.class);
    private final FunctionCallService functionCallService = new FunctionCallServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Function call的正则表达式模式
    private static final Pattern FUNCTION_CALL_PATTERN = Pattern.compile(
            "<function_call>\\s*\\{([^}]+)}\\s*</function_call>"
    );
    
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
    public String chatWithSession(String sessionId, String userId, String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        
        try {
            // 保存用户消息
            LocalDateTime now = LocalDateTime.now();
            chatHistoryDAO.addChatHistory(sessionId, userId, "user", message, null, null, now, now);
            
            // 获取历史记录
            List<ChatHistory> history = chatHistoryDAO.getChatHistoryBySessionId(sessionId);
            List<ChatMessage> chatMessages = convertToChatMessages(history);
            
            // 调用AI
            ChatLanguageModel model = ModelUtil.getLanguageModel();
            String aiResponse = model.chat(chatMessages).toString();
            
            // 保存AI回复
            chatHistoryDAO.addChatHistory(sessionId, userId, "assistant", aiResponse, null, null, now, now);
            
            return aiResponse;
        } catch (Exception e) {
            System.err.println("AI会话服务异常: " + e.getMessage());
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }
    
    @Override
    public String chatWithFunctionCall(String sessionId, String userId, String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        
        try {
            // 保存用户消息
            LocalDateTime now = LocalDateTime.now();
            chatHistoryDAO.addChatHistory(sessionId, userId, "user", message, null, null, now, now);
            
            // 获取历史记录并构建系统提示
            List<ChatHistory> history = chatHistoryDAO.getChatHistoryBySessionId(sessionId);
            List<ChatMessage> chatMessages = convertToChatMessages(history);
            
            // 添加系统提示，告诉AI如何使用函数
            String systemPrompt = buildSystemPromptWithFunctions();
            chatMessages.add(0, UserMessage.from(systemPrompt));
            
            // 调用AI
            ChatLanguageModel model = ModelUtil.getLanguageModel();
            String aiResponse = model.chat(chatMessages).toString();
            
            // 检查是否包含函数调用
            String finalResponse = processFunctionCalls(aiResponse, sessionId, userId);
            
            // 保存最终回复
            chatHistoryDAO.addChatHistory(sessionId, userId, "assistant", finalResponse, null, null, now, now);
            
            return finalResponse;
        } catch (Exception e) {
            System.err.println("AI函数调用服务异常: " + e.getMessage());
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    @SuppressWarnings("unchecked")
    private String buildSystemPromptWithFunctions() {
        Map<String, Object> functions = functionCallService.getAvailableFunctions();
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能助手，可以帮助用户管理学生、教师和班级信息。");
        prompt.append("当用户需要查询、添加、修改或删除数据时，你可以调用以下函数：\n\n");
        
        for (Map.Entry<String, Object> entry : functions.entrySet()) {
            String functionName = entry.getKey();
            Map<String, Object> functionInfo = (Map<String, Object>) entry.getValue();
            prompt.append("函数名: ").append(functionName).append("\n");
            prompt.append("描述: ").append(functionInfo.get("description")).append("\n");
            prompt.append("参数: ").append(functionInfo.get("parameters")).append("\n\n");
        }
        
        prompt.append("调用函数时，请使用以下格式：\n");
        prompt.append("<function_call>{\"function\": \"函数名\", \"parameters\": {参数对象}}</function_call>\n\n");
        prompt.append("请根据用户的需求选择合适的函数进行调用。");
        
        return prompt.toString();
    }

    @SuppressWarnings("unchecked")
    private String processFunctionCalls(String aiResponse, String sessionId, String userId) {
        Matcher matcher = FUNCTION_CALL_PATTERN.matcher(aiResponse);
        StringBuilder result = new StringBuilder(aiResponse);
        
        while (matcher.find()) {
            try {
                String functionCallJson = "{" + matcher.group(1) + "}";
                Map<String, Object> functionCall = objectMapper.readValue(functionCallJson, Map.class);
                
                String functionName = (String) functionCall.get("function");
                Map<String, Object> parameters = (Map<String, Object>) functionCall.get("parameters");
                
                // 执行函数
                Object functionResult = functionCallService.executeFunction(functionName, parameters);
                
                // 保存函数调用记录
                LocalDateTime now = LocalDateTime.now();
                chatHistoryDAO.addChatHistory(sessionId, userId, "function", 
                    "Function: " + functionName, functionCallJson, 
                    objectMapper.writeValueAsString(functionResult), now, now);
                
                // 替换函数调用为结果
                String replacement = "\n\n[执行结果]: " + objectMapper.writeValueAsString(functionResult);
                result = new StringBuilder(result.toString().replace(matcher.group(0), replacement));
                
            } catch (Exception e) {
                System.err.println("函数调用执行失败: " + e.getMessage());
                String errorMsg = "\n\n[执行错误]: " + e.getMessage();
                result = new StringBuilder(result.toString().replace(matcher.group(0), errorMsg));
            }
        }
        
        return result.toString();
    }
    
    private List<ChatMessage> convertToChatMessages(List<ChatHistory> history) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        
        for (ChatHistory chat : history) {
            if ("user".equals(chat.getRole())) {
                chatMessages.add(UserMessage.from(chat.getContent()));
            } else if ("assistant".equals(chat.getRole())) {
                chatMessages.add(AiMessage.from(chat.getContent()));
            }
            // 跳过function类型的消息，因为它们已经被处理过了
        }
        
        return chatMessages;
    }
    
    @Override
    public List<Map<String, Object>> getSessionHistory(String sessionId) {
        List<ChatHistory> history = chatHistoryDAO.getChatHistoryBySessionId(sessionId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ChatHistory chat : history) {
            Map<String, Object> message = new HashMap<>();
            message.put("id", chat.getId());
            message.put("role", chat.getRole());
            message.put("content", chat.getContent());
            message.put("createTime", chat.getCreateTime());
            if (chat.getFunctionCall() != null) {
                message.put("functionCall", chat.getFunctionCall());
            }
            if (chat.getFunctionResult() != null) {
                message.put("functionResult", chat.getFunctionResult());
            }
            result.add(message);
        }
        
        return result;
    }
    
    @Override
    public List<String> getUserSessions(String userId) {
        return chatHistoryDAO.getSessionsByUserId(userId);
    }
    
    @Override
    public boolean deleteSession(String sessionId) {
        return chatHistoryDAO.deleteChatHistoryBySessionId(sessionId);
    }
    
    @Override
    public boolean isAvailable() {
        try {
            ChatLanguageModel model = ModelUtil.getLanguageModel();
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