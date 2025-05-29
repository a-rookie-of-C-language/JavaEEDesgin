package site.arookieofc.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import site.arookieofc.dao.TeacherDAO;
import site.arookieofc.entity.Teacher;
import site.arookieofc.processor.sql.DAOFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceImplTest {

    @Mock
    private TeacherDAO teacherDAO;
    
    private TeacherServiceImpl teacherService;
    
    @BeforeEach
    void setUp() {
        try (MockedStatic<DAOFactory> daoFactoryMock = mockStatic(DAOFactory.class)) {
            daoFactoryMock.when(() -> DAOFactory.getDAO(TeacherDAO.class)).thenReturn(teacherDAO);
            teacherService = new TeacherServiceImpl();
        }
    }
    
    @Test
    @DisplayName("测试获取所有教师成功时调用DAO")
    void testGetAllTeachers_Success_ShouldCallDAO() {
        // Given
        List<Teacher> mockTeachers = Arrays.asList(
            createMockTeacher("T001", "张老师"),
            createMockTeacher("T002", "李老师")
        );
        when(teacherDAO.getAllTeachers()).thenReturn(mockTeachers);
        
        // When
        List<Teacher> result = teacherService.getAllTeachers();
        
        // Then
        assertNotNull(result, "结果不应为空");
        verify(teacherDAO, times(1)).getAllTeachers();
    }
    
    @Test
    void testGetAllTeachers_Exception_ShouldReturnNull() {
        // Given
        when(teacherDAO.getAllTeachers()).thenThrow(new RuntimeException("数据库错误"));
        
        // When
        List<Teacher> result = teacherService.getAllTeachers();
        
        // Then
        assertNull(result, "异常时应返回null");
        verify(teacherDAO, times(1)).getAllTeachers();
    }
    
    @Test
    @DisplayName("测试根据ID获取教师有效ID时调用DAO")
    void testGetTeacherById_ValidId_ShouldCallDAO() {
        // Given
        String teacherId = "T001";
        Teacher mockTeacher = createMockTeacher(teacherId, "测试老师");
        when(teacherDAO.getTeacherById(teacherId)).thenReturn(Optional.of(mockTeacher));
        
        // When
        Optional<Teacher> result = teacherService.getTeacherById(teacherId);
        
        // Then
        assertTrue(result.isPresent(), "应该返回教师信息");
        verify(teacherDAO, times(1)).getTeacherById(teacherId);
    }
    
    @Test
    @DisplayName("测试根据ID获取教师空ID时不调用DAO")
    void testGetTeacherById_EmptyId_ShouldNotCallDAO() {
        // When
        Optional<Teacher> result = teacherService.getTeacherById("");
        
        // Then
        assertFalse(result.isPresent(), "空ID应该返回空结果");
        verify(teacherDAO, never()).getTeacherById(anyString());
    }
    
    @Test
    void testGetTeacherById_Exception_ShouldReturnEmpty() {
        // Given
        String teacherId = "T001";
        when(teacherDAO.getTeacherById(teacherId)).thenThrow(new RuntimeException("数据库错误"));
        
        // When
        Optional<Teacher> result = teacherService.getTeacherById(teacherId);
        
        // Then
        assertFalse(result.isPresent(), "异常时应返回空结果");
        verify(teacherDAO, times(1)).getTeacherById(teacherId);
    }
    
    @Test
    void testGetAllClassNames_Success_ShouldCallDAO() {
        // Given
        List<String> mockClassNames = Arrays.asList("班级1", "班级2");
        when(teacherDAO.getAllClassNames()).thenReturn(mockClassNames);
        
        // When
        List<String> result = teacherService.getAllClassNames();
        
        // Then
        assertNotNull(result, "结果不应为空");
        verify(teacherDAO, times(1)).getAllClassNames();
    }
    
    @Test
    void testGetAllClassNames_Exception_ShouldReturnNull() {
        // Given
        when(teacherDAO.getAllClassNames()).thenThrow(new RuntimeException("数据库错误"));
        
        // When
        List<String> result = teacherService.getAllClassNames();
        
        // Then
        assertNull(result, "异常时应返回null");
        verify(teacherDAO, times(1)).getAllClassNames();
    }
    
    @Test
    void testAddTeacher_ValidTeacher_ShouldCallDAO() {
        // Given
        Teacher teacher = createMockTeacher("T003", "新老师");
        when(teacherDAO.addTeacher(
            eq(teacher.getId()),
            eq(teacher.getName()),
            eq(teacher.getDepartment())
        )).thenReturn(1); // 添加这行，模拟添加成功
        
        // When
        assertDoesNotThrow(() -> teacherService.addTeacher(teacher));
        
        // Then
        verify(teacherDAO, times(1)).addTeacher(
            eq(teacher.getId()),
            eq(teacher.getName()),
            eq(teacher.getDepartment())
        );
    }
    
    @Test
    void testUpdateTeacher_ValidTeacher_ShouldCallDAO() {
        // Given
        Teacher teacher = createMockTeacher("T001", "更新老师");
        when(teacherDAO.updateTeacher(
            eq(teacher.getId()),
            eq(teacher.getName()),
            eq(teacher.getDepartment())
        )).thenReturn(true); // 添加这行，模拟更新成功
        
        // When
        assertDoesNotThrow(() -> teacherService.updateTeacher(teacher));
        
        // Then
        verify(teacherDAO, times(1)).updateTeacher(
            eq(teacher.getId()),
            eq(teacher.getName()),
            eq(teacher.getDepartment())
        );
    }
    
    @Test
    void testDeleteTeacher_ValidId_ShouldCallDAO() {
        // Given
        String teacherId = "T001";
        when(teacherDAO.deleteTeacher(teacherId)).thenReturn(true); // 添加这行，模拟删除成功
        
        // When
        assertDoesNotThrow(() -> teacherService.deleteTeacher(teacherId));
        
        // Then
        verify(teacherDAO, times(1)).deleteTeacher(teacherId);
    }
    
    private Teacher createMockTeacher(String id, String name) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setName(name);
        teacher.setDepartment("测试部门");
        return teacher;
    }
}