package site.arookieofc.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
import site.arookieofc.pojo.dto.PageResult;
import site.arookieofc.pojo.dto.Result;
import site.arookieofc.pojo.dto.StudentDTO;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;
import site.arookieofc.service.impl.StudentServiceImpl;
import site.arookieofc.processor.transaction.TransactionInterceptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService studentService;
    
    @Mock
    private TeacherService teacherService;
    
    private StudentController studentController;
    
    @BeforeEach
    void setUp() {
        try (MockedStatic<TransactionInterceptor> transactionMock = mockStatic(TransactionInterceptor.class)) {
            transactionMock.when(() -> TransactionInterceptor.createProxy(any(StudentServiceImpl.class)))
                          .thenReturn(studentService);
            
            studentController = new StudentController();
            setPrivateField(studentController, "teacherService", teacherService);
        }
    }
    
    @Test
    @DisplayName("测试获取学生列表成功时返回成功结果")
    void testGetStudentList_Success_ShouldReturnSuccessResult() {
        // Given
        int page = 1, size = 10;
        List<Student> mockStudents = Arrays.asList(createMockStudent(1, "学生1", "T001"));
        PageResult<Student> mockPageResult = new PageResult<>(mockStudents, 1, page, size);
        
        when(studentService.getStudentsByPage(page, size)).thenReturn(mockPageResult);
        
        // When
        Result result = studentController.getStudentList(page, size);
        
        // Then
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(studentService, times(1)).getStudentsByPage(page, size);
    }
    
    @Test
    @DisplayName("测试获取学生列表异常时返回错误结果")
    void testGetStudentList_Exception_ShouldReturnErrorResult() {
        // Given
        int page = 1, size = 10;
        when(studentService.getStudentsByPage(page, size)).thenThrow(new RuntimeException("数据库错误"));
        
        // When
        Result result = studentController.getStudentList(page, size);
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertFalse(result.isSuccess(), "应该返回失败结果");
        assertTrue(result.getMsg().contains("获取学生列表失败"));
        
        verify(studentService, times(1)).getStudentsByPage(page, size);
    }
    
    @Test
    @DisplayName("测试添加学生成功时返回成功结果")
    void testAddStudent_Success_ShouldReturnSuccessResult() {
        // Given
        Student newStudent = createMockStudent(null, "新学生", "T001");
        StudentDTO studentDTO = StudentDTO.fromEntity(newStudent);
        // addStudent方法返回void，不需要when().thenReturn()
        doNothing().when(studentService).addStudent(any(Student.class));
        
        // When
        Result result = studentController.addStudent(studentDTO);
        
        // Then
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals(200, result.getCode());
        // 使用包含检查而不是精确匹配
        assertTrue(result.getMsg().contains("成功"), "消息应包含'成功'");
        
        verify(studentService, times(1)).addStudent(any(Student.class));
    }
    
    @Test
    @DisplayName("测试更新学生成功时设置ID并返回成功结果")
    void testUpdateStudent_Success_ShouldSetIdAndReturnSuccess() {
        // Given
        int studentId = 1;
        Student updateStudent = createMockStudent(studentId, "更新学生", "T001");
        StudentDTO studentDTO = StudentDTO.fromEntity(updateStudent);
        // updateStudent方法返回void，不需要when().thenReturn()
        doNothing().when(studentService).updateStudent(any(Student.class));
        
        // When
        Result result = studentController.updateStudent(studentId, studentDTO);
        
        // Then
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals(200, result.getCode());
        // 使用包含检查而不是精确匹配
        assertTrue(result.getMsg().contains("成功"), "消息应包含'成功'");
        
        verify(studentService, times(1)).updateStudent(any(Student.class));
    }
    
    @Test
    @DisplayName("测试删除学生成功时返回成功结果")
    void testDeleteStudent_Success_ShouldReturnSuccessResult() {
        // Given
        int studentId = 1;
        // deleteStudent方法返回void，不需要when().thenReturn()
        doNothing().when(studentService).deleteStudent(studentId);
        
        // When
        Result result = studentController.deleteStudent(studentId);
        
        // Then
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals(200, result.getCode());
        // 使用包含检查而不是精确匹配
        assertTrue(result.getMsg().contains("成功"), "消息应包含'成功'");
        
        verify(studentService, times(1)).deleteStudent(studentId);
    }
    
    @Test
    @DisplayName("测试获取所有学生成功时返回成功结果")
    void testGetAllStudents_Success_ShouldReturnSuccessResult() {
        // Given
        List<Student> mockStudents = Arrays.asList(
            createMockStudent(1, "学生1", "T001"),
            createMockStudent(2, "学生2", "T002")
        );
        when(studentService.getAllStudents()).thenReturn(mockStudents);
        
        // When
        Result result = studentController.getAllStudents();
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertNotNull(result.getData(), "数据不应为空");
        // 不检查具体消息内容，因为getAllStudents方法没有设置具体消息
        
        verify(studentService, times(1)).getAllStudents();
    }
    
    private Student createMockStudent(Integer id, String name, String teacherId) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(20);
        student.setTeacherId(teacherId);
        student.setClazz("测试班级");
        return student;
    }
    
    private Teacher createMockTeacher(String id, String name) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setName(name);
        teacher.setDepartment("测试部门");
        return teacher;
    }
    
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("设置私有字段失败: " + e.getMessage(), e);
        }
    }
}