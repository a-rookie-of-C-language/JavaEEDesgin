package site.arookieofc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatHistory {
    private Long id;
    private String sessionId;  // 会话ID，用于区分不同的对话会话
    private String userId;      // 用户ID（可选，如果有用户系统）
    private String role;        // user, assistant, system
    private String content;     // 消息内容
    private String functionCall; // function call信息（JSON格式）
    private String functionResult; // function执行结果
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}