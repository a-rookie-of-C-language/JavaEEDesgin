package site.arookieofc.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.arookieofc.utils.BusinessToolManager;

import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
public class AiServiceImpl implements AiService {

    @Autowired
    private BusinessToolManager businessToolManager;

    private ObjectMapper objectMapper = new ObjectMapper();
    private boolean initialized = false;

    @PostConstruct
    public void initialize() {
        try {
            log.info("初始化AI服务...");
            initialized = true;
            log.info("AI服务初始化完成");
        } catch (Exception e) {
            log.error("AI服务初始化失败", e);
            throw new RuntimeException("AI服务初始化失败", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            log.info("AI服务正在关闭...");
            initialized = false;
            log.info("AI服务已关闭");
        } catch (Exception e) {
            log.error("AI服务关闭失败", e);
        }
    }

    @Override
    public Flux<String> chatStream(String message) {
        return Flux.create(sink -> {
            try {
                if (!initialized) {
                    sink.error(new RuntimeException("AI服务未初始化"));
                    return;
                }
                
                log.info("处理聊天消息: {}", message);
                
                // 分析消息是否需要调用业务工具
                String response = processMessage(message);
                
                // 流式返回响应
                String[] parts = response.split("\n");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        sink.next(part + "\n");
                        try {
                            Thread.sleep(50); // 模拟流式输出
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                sink.complete();
            } catch (Exception e) {
                log.error("处理聊天消息失败", e);
                sink.error(e);
            }
        });
    }

    private String processMessage(String message) {
        try {
            // 分析消息意图
            String intent = analyzeIntent(message);
            
            if (intent.equals("business_operation")) {
                // 需要执行业务操作
                return executeBusinessOperation(message);
            } else {
                // 普通对话
                return generateResponse(message);
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
            return "抱歉，处理您的请求时出现了错误: " + e.getMessage();
        }
    }

    private String analyzeIntent(String message) {
        // 简单的意图识别逻辑
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("添加") || lowerMessage.contains("新增") || 
            lowerMessage.contains("创建") || lowerMessage.contains("增加")) {
            return "business_operation";
        }
        
        if (lowerMessage.contains("删除") || lowerMessage.contains("移除")) {
            return "business_operation";
        }
        
        if (lowerMessage.contains("修改") || lowerMessage.contains("更新") || 
            lowerMessage.contains("编辑")) {
            return "business_operation";
        }
        
        if (lowerMessage.contains("查询") || lowerMessage.contains("查找") || 
            lowerMessage.contains("搜索") || lowerMessage.contains("获取") ||
            lowerMessage.contains("学生") || lowerMessage.contains("教师") ||
            lowerMessage.contains("班级")) {
            return "business_operation";
        }
        
        return "general_chat";
    }

    private String executeBusinessOperation(String message) {
        try {
            // 确定需要调用的工具
            String toolName = determineToolName(message);
            Map<String, Object> parameters = extractParameters(message);
            
            // 直接调用业务工具管理器执行操作
//            String result = businessToolManager.executeTool(toolName, parameters);
            
            // 处理结果
//            return formatBusinessResult(result, message);
            return null;
            
        } catch (Exception e) {
            log.error("执行业务操作失败", e);
            return "执行业务操作时出现错误: " + e.getMessage();
        }
    }

    private String determineToolName(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("学生")) {
            if (lowerMessage.contains("添加") || lowerMessage.contains("新增")) {
                return "addStudent";
            } else if (lowerMessage.contains("删除")) {
                return "deleteStudent";
            } else if (lowerMessage.contains("修改") || lowerMessage.contains("更新")) {
                return "updateStudent";
            } else {
                return "getStudents";
            }
        }
        
        if (lowerMessage.contains("教师")) {
            if (lowerMessage.contains("添加") || lowerMessage.contains("新增")) {
                return "addTeacher";
            } else if (lowerMessage.contains("删除")) {
                return "deleteTeacher";
            } else if (lowerMessage.contains("修改") || lowerMessage.contains("更新")) {
                return "updateTeacher";
            } else {
                return "getTeachers";
            }
        }
        
        if (lowerMessage.contains("班级")) {
            if (lowerMessage.contains("添加") || lowerMessage.contains("新增")) {
                return "addClass";
            } else if (lowerMessage.contains("删除")) {
                return "deleteClass";
            } else if (lowerMessage.contains("修改") || lowerMessage.contains("更新")) {
                return "updateClass";
            } else {
                return "getClasses";
            }
        }
        
        return "getStudents"; // 默认操作
    }

    private Map<String, Object> extractParameters(String message) {
        // 简单的参数提取逻辑，可以根据实际需求扩展
        Map<String, Object> params = new HashMap<>();
        params.put("message", message);
        
        // 提取常见的参数模式
        String lowerMessage = message.toLowerCase();
        
        // 提取ID参数
        if (lowerMessage.contains("id:") || lowerMessage.contains("编号:")) {
            String[] parts = message.split("(id:|编号:)");
            if (parts.length > 1) {
                String idPart = parts[1].trim().split("\\s+")[0];
                params.put("id", idPart);
            }
        }
        
        // 提取姓名参数
        if (lowerMessage.contains("姓名:") || lowerMessage.contains("名字:")) {
            String[] parts = message.split("(姓名:|名字:)");
            if (parts.length > 1) {
                String namePart = parts[1].trim().split("\\s+")[0];
                params.put("name", namePart);
            }
        }
        
        return params;
    }

    private String formatBusinessResult(String result, String originalMessage) {
        try {
            if (result != null && !result.isEmpty()) {
                return "操作成功！\n" + result;
            } else {
                return "操作完成，但没有返回结果";
            }
        } catch (Exception e) {
            log.error("格式化业务结果失败", e);
            return "操作完成，但结果格式化失败";
        }
    }

    private String generateResponse(String message) {
        // 简单的对话响应生成
        return "您好！我是您的业务助手。我可以帮您管理学生、教师和班级信息。\n" +
               "您可以说：\n" +
               "- 查询所有学生\n" +
               "- 添加新学生\n" +
               "- 删除学生\n" +
               "- 修改学生信息\n" +
               "- 查询教师信息\n" +
               "- 管理班级信息\n" +
               "\n请告诉我您需要什么帮助？";
    }
}