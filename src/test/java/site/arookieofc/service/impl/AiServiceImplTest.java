package site.arookieofc.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import site.arookieofc.dao.ChatHistoryDAO;
import site.arookieofc.dao.StudentDAO;
import site.arookieofc.dao.TeacherDAO;
import site.arookieofc.dao.ClazzDAO;
import site.arookieofc.entity.ChatHistory;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
import site.arookieofc.entity.Clazz;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.service.FunctionCallService;
import site.arookieofc.utils.ModelUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private ChatHistoryDAO chatHistoryDAO;

    @Mock
    private StudentDAO studentDAO;

    @Mock
    private TeacherDAO teacherDAO;

    @Mock
    private ClazzDAO clazzDAO;

    @Mock
    private ChatLanguageModel chatLanguageModel;

    @Mock
    private FunctionCallService functionCallService;

    private AiServiceImpl aiService;
    private ObjectMapper objectMapper;
    private MockedStatic<DAOFactory> daoFactoryMock;
    private MockedStatic<ModelUtil> modelUtilMock;

    @BeforeEach
    void setUp() {
        // 创建静态mock，在每个测试方法中都可用
        daoFactoryMock = mockStatic(DAOFactory.class);
        modelUtilMock = mockStatic(ModelUtil.class);
        
        daoFactoryMock.when(() -> DAOFactory.getDAO(ChatHistoryDAO.class)).thenReturn(chatHistoryDAO);
        daoFactoryMock.when(() -> DAOFactory.getDAO(StudentDAO.class)).thenReturn(studentDAO);
        daoFactoryMock.when(() -> DAOFactory.getDAO(TeacherDAO.class)).thenReturn(teacherDAO);
        daoFactoryMock.when(() -> DAOFactory.getDAO(ClazzDAO.class)).thenReturn(clazzDAO);
        
        // Mock ModelUtil.getLanguageModel() 返回我们的mock对象
        modelUtilMock.when(ModelUtil::getLanguageModel).thenReturn(chatLanguageModel);
        
        aiService = new AiServiceImpl();
        objectMapper = new ObjectMapper();
    }
    
    @AfterEach
    void tearDown() {
        // 清理静态mock
        if (daoFactoryMock != null) {
            daoFactoryMock.close();
        }
        if (modelUtilMock != null) {
            modelUtilMock.close();
        }
    }

    @Test
    @DisplayName("测试简单对话功能")
    void testChat_SimpleMessage_ShouldReturnResponse() {
        // Given
        String message = "你好，AI助手";
        String mockResponse = "你好！我是AI助手，很高兴为您服务。";
        when(chatLanguageModel.chat(eq(message))).thenReturn(mockResponse);

        // When
        String actualResponse = aiService.chat(message);

        // Then
        assertNotNull(actualResponse, "AI响应不应为空");
        assertFalse(actualResponse.trim().isEmpty(), "AI响应不应为空字符串");
        verify(chatLanguageModel, times(1)).chat(eq(message));
    }

    @Test
    @DisplayName("测试空消息应抛出异常")
    void testChat_EmptyMessage_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiService.chat("")
        );
        assertEquals("消息内容不能为空", exception.getMessage());
        verify(chatLanguageModel, never()).chat(anyString());
    }

    @Test
    @DisplayName("测试AI服务异常处理")
    void testChat_AIServiceException_ShouldReturnErrorMessage() {
        // Given
        String message = "测试消息";
        when(chatLanguageModel.chat(eq(message))).thenThrow(new RuntimeException("AI服务连接失败"));

        // When
        String response = aiService.chat(message);

        // Then
        assertEquals("抱歉，AI服务暂时不可用，请稍后再试。", response);
    }

    // ========== 会话记忆功能测试 ==========

    @Test
    @DisplayName("测试带会话ID的对话功能")
    void testChatWithSession_ValidInput_ShouldSaveAndReturnResponse() {
        // Given
        String sessionId = "session-123";
        String userId = "user-456";
        String message = "请介绍一下你自己";
        String aiResponse = "我是一个AI助手，可以帮助您处理各种问题。";

        // 创建ChatResponse对象
        AiMessage aiMessage = new AiMessage(aiResponse);
        ChatResponse mockChatResponse = ChatResponse.builder().aiMessage(aiMessage).build();

        List<ChatHistory> mockHistory = createMockChatHistory(sessionId, userId);
        when(chatHistoryDAO.getChatHistoryBySessionId(sessionId)).thenReturn(mockHistory);
        when(chatLanguageModel.chat(anyList())).thenReturn(mockChatResponse);
        when(chatHistoryDAO.addChatHistory(anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        // When
        String result = aiService.chatWithSession(sessionId, userId, message);

        // Then
        assertEquals(aiResponse, result);
        verify(chatHistoryDAO, times(2)).addChatHistory(anyString(), anyString(), anyString(),
                anyString(), any(), any(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(chatHistoryDAO, times(1)).getChatHistoryBySessionId(sessionId);
    }

    @Test
    @DisplayName("测试获取会话历史记录")
    void testGetSessionHistory_ValidSessionId_ShouldReturnHistory() {
        // Given
        String sessionId = "session-123";
        List<ChatHistory> mockHistory = createMockChatHistory(sessionId, "user-456");
        when(chatHistoryDAO.getChatHistoryBySessionId(sessionId)).thenReturn(mockHistory);

        // When
        List<Map<String, Object>> result = aiService.getSessionHistory(sessionId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user", result.get(0).get("role"));
        assertEquals("assistant", result.get(1).get("role"));
        verify(chatHistoryDAO, times(1)).getChatHistoryBySessionId(sessionId);
    }

    @Test
    @DisplayName("测试获取用户所有会话")
    void testGetUserSessions_ValidUserId_ShouldReturnSessions() {
        // Given
        String userId = "user-456";
        List<String> mockSessions = Arrays.asList("session-1", "session-2", "session-3");
        when(chatHistoryDAO.getSessionsByUserId(userId)).thenReturn(mockSessions);

        // When
        List<String> result = aiService.getUserSessions(userId);

        // Then
        assertEquals(mockSessions, result);
        verify(chatHistoryDAO, times(1)).getSessionsByUserId(userId);
    }

    @Test
    @DisplayName("测试删除会话")
    void testDeleteSession_ValidSessionId_ShouldReturnTrue() {
        // Given
        String sessionId = "session-123";
        when(chatHistoryDAO.deleteChatHistoryBySessionId(sessionId)).thenReturn(true);

        // When
        boolean result = aiService.deleteSession(sessionId);

        // Then
        assertTrue(result);
        verify(chatHistoryDAO, times(1)).deleteChatHistoryBySessionId(sessionId);
    }

    // ========== Function Call CRUD测试 ==========

    @Test
    @DisplayName("测试Function Call - 查询学生")
    void testChatWithFunctionCall_GetStudent_ShouldExecuteFunction() {
        // Given
        String sessionId = "session-123";
        String userId = "user-456";
        String message = "请查询ID为1的学生信息";
        String aiResponseWithFunction = "我来帮您查询学生信息。<function_call>{\"function\": \"get_student_by_id\", \"parameters\": {\"id\": 1}}</function_call>";

        // 创建ChatResponse对象
        AiMessage aiMessage = new AiMessage(aiResponseWithFunction);
        ChatResponse mockChatResponse = ChatResponse.builder().aiMessage(aiMessage).build();

        Student mockStudent = createMockStudent(1, "张三");
        Map<String, Object> functionResult = Map.of("id", 1, "name", "张三", "age", 20);

        when(chatHistoryDAO.getChatHistoryBySessionId(sessionId)).thenReturn(new ArrayList<>());
        when(chatLanguageModel.chat(anyList())).thenReturn(mockChatResponse);
        when(functionCallService.executeFunction("get_student_by_id", Map.of("id", 1)))
                .thenReturn(functionResult);
        when(chatHistoryDAO.addChatHistory(anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        // When
        String result = aiService.chatWithFunctionCall(sessionId, userId, message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("执行结果"));
        verify(functionCallService, times(1)).executeFunction("get_student_by_id", Map.of("id", 1));
    }

    @Test
    @DisplayName("测试Function Call - 添加学生")
    void testChatWithFunctionCall_AddStudent_ShouldExecuteFunction() {
        // Given
        String sessionId = "session-123";
        String userId = "user-456";
        String message = "请添加一个新学生，姓名李四，年龄19岁";
        String aiResponseWithFunction = "我来帮您添加学生。<function_call>{\"function\": \"add_student\", \"parameters\": {\"name\": \"李四\", \"age\": 19}}</function_call>";

        // 创建ChatResponse对象
        AiMessage aiMessage = new AiMessage(aiResponseWithFunction);
        ChatResponse mockChatResponse = ChatResponse.builder().aiMessage(aiMessage).build();

        Map<String, Object> parameters = Map.of("name", "李四", "age", 19);

        when(chatHistoryDAO.getChatHistoryBySessionId(sessionId)).thenReturn(new ArrayList<>());
        when(chatLanguageModel.chat(anyList())).thenReturn(mockChatResponse);
        when(functionCallService.executeFunction("add_student", parameters)).thenReturn(1);
        when(chatHistoryDAO.addChatHistory(anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        // When
        String result = aiService.chatWithFunctionCall(sessionId, userId, message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("执行结果"));
        verify(functionCallService, times(1)).executeFunction("add_student", parameters);
    }

    @Test
    @DisplayName("测试Function Call - 查询所有教师")
    void testChatWithFunctionCall_GetAllTeachers_ShouldExecuteFunction() {
        // Given
        String sessionId = "session-123";
        String userId = "user-456";
        String message = "请显示所有教师信息";
        String aiResponseWithFunction = "我来为您查询所有教师信息。<function_call>{\"function\": \"get_all_teachers\", \"parameters\": {}}</function_call>";

        // 创建ChatResponse对象
        AiMessage aiMessage = new AiMessage(aiResponseWithFunction);
        ChatResponse mockChatResponse = ChatResponse.builder().aiMessage(aiMessage).build();

        List<Teacher> mockTeachers = Arrays.asList(
                createMockTeacher("T001", "王老师"),
                createMockTeacher("T002", "李老师")
        );

        when(chatHistoryDAO.getChatHistoryBySessionId(sessionId)).thenReturn(new ArrayList<>());
        when(chatLanguageModel.chat(anyList())).thenReturn(mockChatResponse);
        when(functionCallService.executeFunction("get_all_teachers", Map.of())).thenReturn(mockTeachers);
        when(chatHistoryDAO.addChatHistory(anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        // When
        String result = aiService.chatWithFunctionCall(sessionId, userId, message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("执行结果"));
        verify(functionCallService, times(1)).executeFunction("get_all_teachers", Map.of());
    }

    @Test
    @DisplayName("测试Function Call - 函数执行异常")
    void testChatWithFunctionCall_FunctionException_ShouldHandleError() {
        // Given
        String sessionId = "session-123";
        String userId = "user-456";
        String message = "查询不存在的学生";
        String aiResponseWithFunction = "<function_call>{\"function\": \"get_student_by_id\", \"parameters\": {\"id\": 999}}</function_call>";

        // 创建ChatResponse对象
        AiMessage aiMessage = new AiMessage(aiResponseWithFunction);
        ChatResponse mockChatResponse = ChatResponse.builder().aiMessage(aiMessage).build();

        when(chatHistoryDAO.getChatHistoryBySessionId(sessionId)).thenReturn(new ArrayList<>());
        when(chatLanguageModel.chat(anyList())).thenReturn(mockChatResponse);
        when(functionCallService.executeFunction("get_student_by_id", Map.of("id", 999)))
                .thenThrow(new RuntimeException("学生不存在"));
        when(chatHistoryDAO.addChatHistory(anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        // When
        String result = aiService.chatWithFunctionCall(sessionId, userId, message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("执行错误"));
        assertTrue(result.contains("学生不存在"));
    }

    // ========== 服务状态测试 ==========

    @Test
    @DisplayName("测试AI服务可用性检查 - 可用")
    void testIsAvailable_ServiceWorking_ShouldReturnTrue() {
        // Given
        when(chatLanguageModel.chat(eq("hello"))).thenReturn("Hello! How can I help you?");

        // When
        boolean result = aiService.isAvailable();

        // Then
        assertTrue(result);
        verify(chatLanguageModel, times(1)).chat(eq("hello"));
    }

    @Test
    @DisplayName("测试AI服务可用性检查 - 不可用")
    void testIsAvailable_ServiceDown_ShouldReturnFalse() {
        // Given
        when(chatLanguageModel.chat(eq("hello"))).thenThrow(new RuntimeException("连接失败"));

        // When
        boolean result = aiService.isAvailable();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试获取模型信息")
    void testGetModelInfo_ShouldReturnModelInfo() {
        // When
        Map<String, String> modelInfo = aiService.getModelInfo();

        // Then
        assertNotNull(modelInfo);
        assertTrue(modelInfo.containsKey("url"));
        assertTrue(modelInfo.containsKey("modelName"));
        assertTrue(modelInfo.containsKey("status"));
    }

    // ========== 历史对话测试 ==========

    @Test
    @DisplayName("测试多轮对话历史")
    void testChatWithHistory_ValidMessages_ShouldReturnResponse() {
        // Given
        List<Map<String, String>> messages = Arrays.asList(
                Map.of("role", "user", "content", "你好"),
                Map.of("role", "assistant", "content", "你好！有什么可以帮助您的吗？"),
                Map.of("role", "user", "content", "请介绍一下你的功能")
        );
        String expectedResponse = "我可以帮助您进行对话、查询和管理学生、教师、班级信息等。";

        // 创建ChatResponse对象
        AiMessage aiMessage = new AiMessage(expectedResponse);
        ChatResponse mockChatResponse = ChatResponse.builder().aiMessage(aiMessage).build();

        when(chatLanguageModel.chat(anyList())).thenReturn(mockChatResponse);

        // When
        String result = aiService.chatWithHistory(messages);

        // Then
        assertEquals(expectedResponse, result);
        verify(chatLanguageModel, times(1)).chat(anyList());
    }

    @Test
    @DisplayName("测试空历史消息应抛出异常")
    void testChatWithHistory_EmptyMessages_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiService.chatWithHistory(new ArrayList<>())
        );
        assertEquals("对话历史不能为空", exception.getMessage());
    }

    // ========== 辅助方法 ==========

    private List<ChatHistory> createMockChatHistory(String sessionId, String userId) {
        List<ChatHistory> history = new ArrayList<>();

        ChatHistory userMessage = new ChatHistory();
        userMessage.setId(1L);
        userMessage.setSessionId(sessionId);
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setContent("你好");
        userMessage.setCreateTime(LocalDateTime.now().minusMinutes(5));

        ChatHistory aiMessage = new ChatHistory();
        aiMessage.setId(2L);
        aiMessage.setSessionId(sessionId);
        aiMessage.setUserId(userId);
        aiMessage.setRole("assistant");
        aiMessage.setContent("你好！有什么可以帮助您的吗？");
        aiMessage.setCreateTime(LocalDateTime.now().minusMinutes(4));

        history.add(userMessage);
        history.add(aiMessage);

        return history;
    }

    private Student createMockStudent(Integer id, String name) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(20);
        student.setTeacherId("T001");
        student.setClazz("计算机1班");
        return student;
    }

    private Teacher createMockTeacher(String id, String name) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setName(name);
        teacher.setDepartment("计算机系");
        return teacher;
    }

    private Clazz createMockClazz(String id, String name) {
        Clazz clazz = new Clazz();
        clazz.setId(id);
        clazz.setName(name);
        clazz.setTeacherId("T001");
        clazz.setDescription("测试班级");
        return clazz;
    }
}