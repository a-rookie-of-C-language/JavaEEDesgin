package site.arookieofc.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import site.arookieofc.dao.StudentDAO;
import site.arookieofc.entity.Student;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.pojo.dto.PageResult;
import site.arookieofc.service.ClazzService;
import site.arookieofc.processor.transaction.TransactionInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentDAO studentDAO;
    
    @Mock
    private ClazzService clazzService;
    
    private StudentServiceImpl studentService;
    
    @BeforeEach
    void setUp() {
        try (MockedStatic<DAOFactory> daoFactoryMock = mockStatic(DAOFactory.class);
             MockedStatic<TransactionInterceptor> transactionMock = mockStatic(TransactionInterceptor.class)) {
            
            daoFactoryMock.when(() -> DAOFactory.getDAO(StudentDAO.class)).thenReturn(studentDAO);
            transactionMock.when(() -> TransactionInterceptor.createProxy(any(ClazzServiceImpl.class)))
                          .thenReturn(clazzService);
            
            studentService = new StudentServiceImpl();
        }
    }
    
    @Test
    @DisplayName("测试根据ID获取学生有效ID时调用DAO")
    void testGetStudentById_ValidId_ShouldCallDAO() {
        // Given
        int studentId = 1;
        Student mockStudent = createMockStudent(studentId, "测试学生");
        when(studentDAO.getStudentById(studentId)).thenReturn(Optional.of(mockStudent));
        
        // When
        Optional<Student> result = studentService.getStudentById(studentId);
        
        // Then
        assertTrue(result.isPresent(), "应该返回学生信息");
        verify(studentDAO, times(1)).getStudentById(studentId);
    }
    
    @Test
    @DisplayName("测试根据ID获取学生无效ID时不调用DAO")
    void testGetStudentById_InvalidId_ShouldNotCallDAO() {
        // When
        Optional<Student> result = studentService.getStudentById(-1);
        
        // Then
        assertFalse(result.isPresent(), "无效ID应该返回空结果");
        verify(studentDAO, never()).getStudentById(anyInt());
    }
    
    @Test
    @DisplayName("测试添加学生有效学生时调用DAO并更新计数")
    void testAddStudent_ValidStudent_ShouldCallDAOAndUpdateCount() {
        // Given
        Student student = createMockStudent(null, "新学生");
        
        // When
        assertDoesNotThrow(() -> studentService.addStudent(student));
        
        // Then
        verify(studentDAO, times(1)).addStudent(
            eq(student.getName()), 
            eq(student.getAge()), 
            eq(student.getTeacherId()), 
            eq(student.getClazz())
        );
        verify(clazzService, times(1)).updateStudentCount(student.getClazz(), 1);
    }
    
    @Test
    @DisplayName("测试添加学生空学生时抛出异常")
    void testAddStudent_NullStudent_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> studentService.addStudent(null)
        );
        assertEquals("学生信息不能为空", exception.getMessage());
        verify(studentDAO, never()).addStudent(anyString(), anyInt(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("测试添加学生空名称时抛出异常")
    void testAddStudent_EmptyName_ShouldThrowException() {
        // Given
        Student student = createMockStudent(null, "");
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> studentService.addStudent(student)
        );
        assertEquals("学生姓名不能为空", exception.getMessage());
        verify(studentDAO, never()).addStudent(anyString(), anyInt(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("测试添加学生无效年龄时抛出异常")
    void testAddStudent_InvalidAge_ShouldThrowException() {
        // Given
        Student student = createMockStudent(null, "测试学生");
        student.setAge(0);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> studentService.addStudent(student)
        );
        assertEquals("学生年龄必须在1-150之间", exception.getMessage());
        verify(studentDAO, never()).addStudent(anyString(), anyInt(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("测试更新学生有效学生时调用DAO并更新计数")
    void testUpdateStudent_ValidStudent_ShouldCallDAOAndUpdateCount() {
        // Given
        Student originalStudent = createMockStudent(1, "原学生");
        originalStudent.setClazz("旧班级");
        
        Student newStudent = createMockStudent(1, "新学生");
        newStudent.setClazz("新班级");
        
        when(studentDAO.getStudentById(1)).thenReturn(Optional.of(originalStudent));
        when(studentDAO.updateStudent(anyString(), anyInt(), anyString(), anyString(), anyInt()))
            .thenReturn(true);
        
        // When
        assertDoesNotThrow(() -> studentService.updateStudent(newStudent));
        
        // Then
        verify(studentDAO, times(1)).updateStudent(
            eq(newStudent.getName()),
            eq(newStudent.getAge()),
            eq(newStudent.getTeacherId()),
            eq(newStudent.getClazz()),
            eq(newStudent.getId())
        );
        // 验证班级变更时的学生数量更新
        verify(clazzService, times(1)).updateStudentCount("旧班级", -1);
        verify(clazzService, times(1)).updateStudentCount("新班级", 1);
    }
    
    @Test
    @DisplayName("测试删除学生有效ID时调用DAO并更新计数")
    void testDeleteStudent_ValidId_ShouldCallDAOAndUpdateCount() {
        // Given
        int studentId = 1;
        Student student = createMockStudent(studentId, "测试学生");
        
        when(studentDAO.getStudentById(studentId)).thenReturn(Optional.of(student));
        when(studentDAO.deleteStudent(studentId)).thenReturn(true); // 添加这一行，模拟返回true表示删除成功
        
        // When
        assertDoesNotThrow(() -> studentService.deleteStudent(studentId));
        
        // Then
        verify(studentDAO, times(1)).deleteStudent(studentId);
        verify(clazzService, times(1)).updateStudentCount(student.getClazz(), -1);
    }
    
    @Test
    @DisplayName("测试删除学生无效ID时抛出异常")
    void testDeleteStudent_InvalidId_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> studentService.deleteStudent(-1)
        );
        assertEquals("无效的学生ID", exception.getMessage());
        verify(studentDAO, never()).deleteStudent(anyInt());
    }
    
    @Test
    @DisplayName("测试获取所有学生时调用DAO")
    void testGetAllStudents_ShouldCallDAO() {
        // Given
        List<Student> mockStudents = Arrays.asList(
            createMockStudent(1, "学生1"),
            createMockStudent(2, "学生2")
        );
        when(studentDAO.getAllStudents()).thenReturn(mockStudents);
        
        // When
        List<Student> result = studentService.getAllStudents();
        
        // Then
        assertNotNull(result, "结果不应为空");
        verify(studentDAO, times(1)).getAllStudents();
    }
    
    @Test
    @DisplayName("测试分页获取学生时调用DAO")
    void testGetStudentsByPage_ShouldCallDAO() {
        // Given
        int page = 1, size = 10;
        List<Student> mockStudents = Arrays.asList(createMockStudent(1, "学生1"));
        
        // 注意：StudentServiceImpl实际上是通过getAllStudents()来实现分页的
        when(studentDAO.getAllStudents()).thenReturn(mockStudents);
        
        // When
        PageResult<Student> result = studentService.getStudentsByPage(page, size);
        
        // Then
        assertNotNull(result, "分页结果不应为空");
        verify(studentDAO, times(1)).getAllStudents();
    }
    
    private Student createMockStudent(Integer id, String name) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(20);
        student.setClazz("测试班级");
        student.setTeacherId("T001");
        return student;
    }
}