package site.arookieofc.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import site.arookieofc.dao.StudentDAO;
import site.arookieofc.dao.TeacherDAO;
import site.arookieofc.dao.ClazzDAO;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
import site.arookieofc.entity.Clazz;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.service.FunctionCallService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FunctionCallServiceImplTest {

    @Mock
    private StudentDAO studentDAO;
    
    @Mock
    private TeacherDAO teacherDAO;
    
    @Mock
    private ClazzDAO clazzDAO;
    
    private FunctionCallServiceImpl functionCallService;
    
    @BeforeEach
    void setUp() {
        try (MockedStatic<DAOFactory> daoFactoryMock = mockStatic(DAOFactory.class)) {
            daoFactoryMock.when(() -> DAOFactory.getDAO(StudentDAO.class)).thenReturn(studentDAO);
            daoFactoryMock.when(() -> DAOFactory.getDAO(TeacherDAO.class)).thenReturn(teacherDAO);
            daoFactoryMock.when(() -> DAOFactory.getDAO(ClazzDAO.class)).thenReturn(clazzDAO);
            
            functionCallService = new FunctionCallServiceImpl();
        }
    }
    
    // ========== 学生CRUD测试 ==========
    
    @Test
    @DisplayName("测试根据ID获取学生")
    void testExecuteFunction_GetStudentById_ShouldReturnStudent() {
        // Given
        int studentId = 1;
        Student mockStudent = createMockStudent(studentId, "张三");
        when(studentDAO.getStudentById(studentId)).thenReturn(Optional.of(mockStudent));
        
        Map<String, Object> parameters = Map.of("id", studentId);
        
        // When
        Object result = functionCallService.executeFunction("get_student_by_id", parameters);
        
        // Then
        assertNotNull(result);
        assertEquals(mockStudent, result);
        verify(studentDAO, times(1)).getStudentById(studentId);
    }
    
    @Test
    @DisplayName("测试获取所有学生")
    void testExecuteFunction_GetAllStudents_ShouldReturnStudentList() {
        // Given
        List<Student> mockStudents = Arrays.asList(
            createMockStudent(1, "张三"),
            createMockStudent(2, "李四")
        );
        when(studentDAO.getAllStudents()).thenReturn(mockStudents);
        
        // When
        Object result = functionCallService.executeFunction("get_all_students", Map.of());
        
        // Then
        assertNotNull(result);
        assertEquals(mockStudents, result);
        verify(studentDAO, times(1)).getAllStudents();
    }
    
    @Test
    @DisplayName("测试添加学生")
    void testExecuteFunction_AddStudent_ShouldReturnInsertCount() {
        // Given
        Map<String, Object> parameters = Map.of(
            "name", "王五",
            "age", 21,
            "teacherId", "T001",
            "clazz", "计算机2班"
        );
        when(studentDAO.addStudent("王五", 21, "T001", "计算机2班")).thenReturn(1);
        
        // When
        Object result = functionCallService.executeFunction("add_student", parameters);
        
        // Then
        assertEquals(1, result);
        verify(studentDAO, times(1)).addStudent("王五", 21, "T001", "计算机2班");
    }
    
    @Test
    @DisplayName("测试更新学生")
    void testExecuteFunction_UpdateStudent_ShouldReturnUpdateResult() {
        // Given
        Map<String, Object> parameters = Map.of(
            "id", 1,
            "name", "张三修改",
            "age", 22,
            "teacherId", "T002",
            "clazz", "计算机3班"
        );
        when(studentDAO.updateStudent("张三修改", 22, "T002", "计算机3班", 1)).thenReturn(true);
        
        // When
        Object result = functionCallService.executeFunction("update_student", parameters);
        
        // Then
        assertEquals(true, result);
        verify(studentDAO, times(1)).updateStudent("张三修改", 22, "T002", "计算机3班", 1);
    }
    
    @Test
    @DisplayName("测试删除学生")
    void testExecuteFunction_DeleteStudent_ShouldReturnDeleteResult() {
        // Given
        Map<String, Object> parameters = Map.of("id", 1);
        when(studentDAO.deleteStudent(1)).thenReturn(true);
        
        // When
        Object result = functionCallService.executeFunction("delete_student", parameters);
        
        // Then
        assertEquals(true, result);
        verify(studentDAO, times(1)).deleteStudent(1);
    }
    
    // ========== 教师CRUD测试 ==========
    
    @Test
    @DisplayName("测试根据ID获取教师")
    void testExecuteFunction_GetTeacherById_ShouldReturnTeacher() {
        // Given
        String teacherId = "T001";
        Teacher mockTeacher = createMockTeacher(teacherId, "王老师");
        when(teacherDAO.getTeacherById(teacherId)).thenReturn(Optional.of(mockTeacher));
        
        Map<String, Object> parameters = Map.of("id", teacherId);
        
        // When
        Object result = functionCallService.executeFunction("get_teacher_by_id", parameters);
        
        // Then
        assertNotNull(result);
        assertEquals(mockTeacher, result);
        verify(teacherDAO, times(1)).getTeacherById(teacherId);
    }
    
    @Test
    @DisplayName("测试获取所有教师")
    void testExecuteFunction_GetAllTeachers_ShouldReturnTeacherList() {
        // Given
        List<Teacher> mockTeachers = Arrays.asList(
            createMockTeacher("T001", "王老师"),
            createMockTeacher("T002", "李老师")
        );
        when(teacherDAO.getAllTeachers()).thenReturn(mockTeachers);
        
        // When
        Object result = functionCallService.executeFunction("get_all_teachers", Map.of());
        
        // Then
        assertNotNull(result);
        assertEquals(mockTeachers, result);
        verify(teacherDAO, times(1)).getAllTeachers();
    }
    
    @Test
    @DisplayName("测试添加教师")
    void testExecuteFunction_AddTeacher_ShouldReturnInsertCount() {
        // Given
        Map<String, Object> parameters = Map.of(
            "id", "T003",
            "name", "赵老师",
            "department", "数学系"
        );
        when(teacherDAO.addTeacher("T003", "赵老师", "数学系")).thenReturn(1);
        
        // When
        Object result = functionCallService.executeFunction("add_teacher", parameters);
        
        // Then
        assertEquals(1, result);
        verify(teacherDAO, times(1)).addTeacher("T003", "赵老师", "数学系");
    }
    
    // ========== 班级CRUD测试 ==========
    
    @Test
    @DisplayName("测试根据ID获取班级")
    void testExecuteFunction_GetClassById_ShouldReturnClass() {
        // Given
        String classId = "C001";
        Clazz mockClazz = createMockClazz(classId, "计算机1班");
        when(clazzDAO.getClassById(classId)).thenReturn(Optional.of(mockClazz));
        
        Map<String, Object> parameters = Map.of("id", classId);
        
        // When
        Object result = functionCallService.executeFunction("get_class_by_id", parameters);
        
        // Then
        assertNotNull(result);
        assertEquals(mockClazz, result);
        verify(clazzDAO, times(1)).getClassById(classId);
    }
    
    @Test
    @DisplayName("测试获取所有班级")
    void testExecuteFunction_GetAllClasses_ShouldReturnClassList() {
        // Given
        List<Clazz> mockClasses = Arrays.asList(
            createMockClazz("C001", "计算机1班"),
            createMockClazz("C002", "计算机2班")
        );
        when(clazzDAO.getAllClasses()).thenReturn(mockClasses);
        
        // When
        Object result = functionCallService.executeFunction("get_all_classes", Map.of());
        
        // Then
        assertNotNull(result);
        assertEquals(mockClasses, result);
        verify(clazzDAO, times(1)).getAllClasses();
    }
    
    @Test
    @DisplayName("测试添加班级")
    void testExecuteFunction_AddClass_ShouldReturnInsertCount() {
        // Given
        Map<String, Object> parameters = Map.of(
            "id", "C003",
            "name", "计算机3班",
            "teacherId", "T001",
            "description", "新建班级"
        );
        when(clazzDAO.addClass("C003", "计算机3班", "T001", "新建班级")).thenReturn(1);
        
        // When
        Object result = functionCallService.executeFunction("add_class", parameters);
        
        // Then
        assertEquals(1, result);
        verify(clazzDAO, times(1)).addClass("C003", "计算机3班", "T001", "新建班级");
    }
    
    // ========== 异常处理测试 ==========
    
    @Test
    @DisplayName("测试未知函数应抛出异常")
    void testExecuteFunction_UnknownFunction_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> functionCallService.executeFunction("unknown_function", Map.of())
        );
        assertEquals("未知的函数: unknown_function", exception.getMessage());
    }
    
    @Test
    @DisplayName("测试函数执行异常")
    void testExecuteFunction_ExecutionException_ShouldThrowRuntimeException() {
        // Given
        when(studentDAO.getStudentById(1)).thenThrow(new RuntimeException("数据库连接失败"));
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> functionCallService.executeFunction("get_student_by_id", Map.of("id", 1))
        );
        assertTrue(exception.getMessage().contains("执行函数失败: get_student_by_id"));
    }
    
    // ========== 获取可用函数测试 ==========
    
    @Test
    @DisplayName("测试获取可用函数列表")
    void testGetAvailableFunctions_ShouldReturnFunctionList() {
        // When
        Map<String, Object> functions = functionCallService.getAvailableFunctions();
        
        // Then
        assertNotNull(functions);
        assertTrue(functions.containsKey("get_student_by_id"));
        assertTrue(functions.containsKey("get_all_students"));
        assertTrue(functions.containsKey("add_student"));
        assertTrue(functions.containsKey("get_teacher_by_id"));
        assertTrue(functions.containsKey("get_all_teachers"));
        assertTrue(functions.containsKey("add_teacher"));
        assertTrue(functions.containsKey("get_class_by_id"));
        assertTrue(functions.containsKey("get_all_classes"));
        assertTrue(functions.containsKey("add_class"));
        
        // 验证函数描述信息
        Map<String, Object> getStudentFunction = (Map<String, Object>) functions.get("get_student_by_id");
        assertEquals("根据ID获取学生信息", getStudentFunction.get("description"));
        assertNotNull(getStudentFunction.get("parameters"));
    }
    
    // ========== 注册函数测试 ==========
    
    @Test
    @DisplayName("测试注册自定义函数")
    void testRegisterFunction_ValidFunction_ShouldRegisterSuccessfully() {
        // Given
        String functionName = "custom_function";
        java.util.function.Function<Map<String, Object>, Object> customFunction = 
            params -> "Custom result: " + params.get("input");
        
        // When
        functionCallService.registerFunction(functionName, customFunction);
        Object result = functionCallService.executeFunction(functionName, Map.of("input", "test"));
        
        // Then
        assertEquals("Custom result: test", result);
    }
    
    @Test
    @DisplayName("测试注册无效函数应抛出异常")
    void testRegisterFunction_InvalidFunction_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> functionCallService.registerFunction("invalid", "not a function")
        );
        assertEquals("函数处理器必须是Function类型", exception.getMessage());
    }
    
    // ========== 辅助方法 ==========
    
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