package site.arookieofc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.arookieofc.pojo.dto.Result;
import site.arookieofc.service.AiService;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private AiService aiService;
    
    private AiController aiController;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        aiController = new AiController();
        setPrivateField(aiController, "aiService", aiService);
        objectMapper = new ObjectMapper();
    }
    
    // ========== 基础对话测试 ==========
    
    @Test
    @DisplayName("测试简单对话成功")
    void testChat_ValidMessage_ShouldReturnSuccessResult() {
        // Given
        Map<String, String> request = Map.of("message", "你好，AI助手");
        String aiResponse = "你好！我是AI助手，很高兴为您服务。";
        when(aiService.chat("你好，AI助手")).thenReturn(aiResponse);
        
        // When
        Result result = aiController.chat(request);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("对话成功", result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(aiResponse, data.get("response"));
        verify(aiService, times(1)).chat("你好，AI助手");
    }
    
    @Test
    @DisplayName("测试空消息应返回错误")
    void testChat_EmptyMessage_ShouldReturnError() {
        // Given
        Map<String, String> request = Map.of("message", "");
        
        // When
        Result result = aiController.chat(request);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("消息内容不能为空", result.getMsg());
        verify(aiService, never()).chat(anyString());
    }
    
    @Test
    @DisplayName("测试AI服务异常应返回错误")
    void testChat_ServiceException_ShouldReturnError() {
        // Given
        Map<String, String> request = Map.of("message", "测试消息");
        when(aiService.chat("测试消息")).thenThrow(new RuntimeException("AI服务连接失败"));
        
        // When
        Result result = aiController.chat(request);
        
        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("AI对话失败"));
    }
    
    // ========== 会话对话测试 ==========
    
    @Test
    @DisplayName("测试会话对话成功")
    void testChatWithSession_ValidInput_ShouldReturnSuccessResult() {
        // Given
        Map<String, String> request = Map.of(
            "sessionId", "session-123",
            "userId", "user-456",
            "message", "请介绍一下你自己"
        );
        String aiResponse = "我是一个AI助手，可以帮助您处理各种问题。";
        when(aiService.chatWithSession("session-123", "user-456", "请介绍一下你自己"))
            .thenReturn(aiResponse);
        
        // When
        Result result = aiController.chatWithSession(request);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("会话对话成功", result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(aiResponse, data.get("response"));
        assertEquals("session-123", data.get("sessionId"));
    }
    
    @Test
    @DisplayName("测试无会话ID时自动生成")
    void testChatWithSession_NoSessionId_ShouldGenerateSessionId() {
        // Given
        Map<String, String> request = Map.of(
            "userId", "user-456",
            "message", "你好"
        );
        String aiResponse = "你好！";
        when(aiService.chatWithSession(anyString(), eq("user-456"), eq("你好")))
            .thenReturn(aiResponse);
        
        // When
        Result result = aiController.chatWithSession(request);
        
        // Then
        assertTrue(result.isSuccess());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("sessionId"));
        assertFalse(((String) data.get("sessionId")).isEmpty());
    }
    
    // ========== Function Call测试 ==========
    
    @Test
    @DisplayName("测试Function Call对话成功")
    void testChatWithFunction_ValidInput_ShouldReturnSuccessResult() {
        // Given
        Map<String, String> request = Map.of(
            "sessionId", "session-123",
            "userId", "user-456",
            "message", "请查询ID为1的学生信息"
        );
        String aiResponse = "学生信息：张三，20岁，计算机1班";
        when(aiService.chatWithFunctionCall("session-123", "user-456", "请查询ID为1的学生信息"))
            .thenReturn(aiResponse);
        
        // When
        Result result = aiController.chatWithFunction(request);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("函数调用对话成功", result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(aiResponse, data.get("response"));
        assertEquals("session-123", data.get("sessionId"));
    }
    
    // ========== 历史记录测试 ==========
    
    @Test
    @DisplayName("测试获取会话历史记录")
    void testGetSessionHistory_ValidSessionId_ShouldReturnHistory() {
        // Given
        String sessionId = "session-123";
        List<Map<String, Object>> mockHistory = Arrays.asList(
            Map.of("id", 1L, "role", "user", "content", "你好"),
            Map.of("id", 2L, "role", "assistant", "content", "你好！有什么可以帮助您的吗？")
        );
        when(aiService.getSessionHistory(sessionId)).thenReturn(mockHistory);
        
        // When
        Result result = aiController.getSessionHistory(sessionId);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("获取历史记录成功", result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(mockHistory, data.get("history"));
    }
    
    @Test
    @DisplayName("测试获取用户会话列表")
    void testGetUserSessions_ValidUserId_ShouldReturnSessions() {
        // Given
        String userId = "user-456";
        List<String> mockSessions = Arrays.asList("session-1", "session-2", "session-3");
        when(aiService.getUserSessions(userId)).thenReturn(mockSessions);
        
        // When
        Result result = aiController.getUserSessions(userId);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("获取用户会话成功", result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(mockSessions, data.get("sessions"));
    }
    
    @Test
    @DisplayName("测试删除会话成功")
    void testDeleteSession_ValidSessionId_ShouldReturnSuccess() {
        // Given
        String sessionId = "session-123";
        when(aiService.deleteSession(sessionId)).thenReturn(true);
        
        // When
        Result result = aiController.deleteSession(sessionId);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("删除会话成功", result.getMsg());
    }
    
    @Test
    @DisplayName("测试删除会话失败")
    void testDeleteSession_FailedDeletion_ShouldReturnError() {
        // Given
        String sessionId = "session-123";
        when(aiService.deleteSession(sessionId)).thenReturn(false);
        
        // When
        Result result = aiController.deleteSession(sessionId);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("删除会话失败", result.getMsg());
    }
    
    // ========== 历史对话测试 ==========
    
    @Test
    @DisplayName("测试多轮历史对话")
    void testChatWithHistory_ValidMessages_ShouldReturnSuccessResult() {
        // Given
        List<Map<String, String>> messages = Arrays.asList(
            Map.of("role", "user", "content", "你好"),
            Map.of("role", "assistant", "content", "你好！有什么可以帮助您的吗？"),
            Map.of("role", "user", "content", "请介绍一下你的功能")
        );
        Map<String, Object> request = Map.of("messages", messages);
        String aiResponse = "我可以帮助您进行对话、查询和管理学生、教师、班级信息等。";
        when(aiService.chatWithHistory(messages)).thenReturn(aiResponse);
        
        // When
        Result result = aiController.chatWithHistory(request);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("多轮对话成功", result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(aiResponse, data.get("response"));
    }
    
    @Test
    @DisplayName("测试空历史消息应返回错误")
    void testChatWithHistory_EmptyMessages_ShouldReturnError() {
        // Given
        Map<String, Object> request = Map.of("messages", new ArrayList<>());
        
        // When
        Result result = aiController.chatWithHistory(request);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("对话历史不能为空", result.getMsg());
    }
    
    // ========== 状态检查测试 ==========
    
    @Test
    @DisplayName("测试获取AI服务状态")
    void testGetStatus_ShouldReturnStatus() {
        // Given
        Map<String, String> mockModelInfo = Map.of(
            "url", "http://localhost:11434",
            "modelName", "llama2",
            "status", "可用"
        );
        when(aiService.isAvailable()).thenReturn(true);
        when(aiService.getModelInfo()).thenReturn(mockModelInfo);
        
        // When
        Result result = aiController.getStatus();
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("状态检查完成", result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(true, data.get("available"));
        assertEquals(mockModelInfo, data.get("modelInfo"));
    }
    
    @Test
    @DisplayName("测试健康检查")
    void testHealth_ShouldReturnHealthStatus() {
        // When
        Result result = aiController.health();
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("AI服务正常运行", result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("timestamp"));
        assertEquals("healthy", data.get("status"));
    }
    
    // ========== 辅助方法 ==========
    
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("设置私有字段失败: " + fieldName, e);
        }
    }
}