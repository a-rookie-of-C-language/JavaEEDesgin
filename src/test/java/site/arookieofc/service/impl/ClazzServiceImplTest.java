package site.arookieofc.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import site.arookieofc.dao.ClazzDAO;
import site.arookieofc.entity.Clazz;
import site.arookieofc.processor.sql.DAOFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClazzServiceImplTest {

    @Mock
    private ClazzDAO clazzDAO;
    
    private ClazzServiceImpl clazzService;
    
    @BeforeEach
    void setUp() {
        try (MockedStatic<DAOFactory> daoFactoryMock = mockStatic(DAOFactory.class)) {
            daoFactoryMock.when(() -> DAOFactory.getDAO(ClazzDAO.class)).thenReturn(clazzDAO);
            clazzService = new ClazzServiceImpl();
        }
    }
    
    @Test
    @DisplayName("测试获取所有班级成功时调用DAO")
    void testGetAllClasses_Success_ShouldCallDAO() {
        // Given
        List<Clazz> mockClasses = Arrays.asList(
            createMockClazz("C001", "班级1"),
            createMockClazz("C002", "班级2")
        );
        when(clazzDAO.getAllClasses()).thenReturn(mockClasses);
        
        // When
        List<Clazz> result = clazzService.getAllClasses();
        
        // Then
        assertNotNull(result, "结果不应为空");
        verify(clazzDAO, times(1)).getAllClasses();
    }
    
    @Test
    void testGetAllClasses_Exception_ShouldReturnEmptyList() {
        // Given
        when(clazzDAO.getAllClasses()).thenThrow(new RuntimeException("数据库错误"));
        
        // When
        List<Clazz> result = clazzService.getAllClasses();
        
        // Then
        assertNotNull(result, "异常时应返回空列表而不是null");
        assertTrue(result.isEmpty(), "异常时应返回空列表");
        verify(clazzDAO, times(1)).getAllClasses();
    }
    
    @Test
    @DisplayName("测试根据ID获取班级有效ID时调用DAO")
    void testGetClassById_ValidId_ShouldCallDAO() {
        // Given
        String classId = "C001";
        Clazz mockClazz = createMockClazz(classId, "测试班级");
        when(clazzDAO.getClassById(classId)).thenReturn(Optional.of(mockClazz));
        
        // When
        Optional<Clazz> result = clazzService.getClassById(classId);
        
        // Then
        assertTrue(result.isPresent(), "应该返回班级信息");
        verify(clazzDAO, times(1)).getClassById(classId);
    }
    
    @Test
    @DisplayName("测试根据ID获取班级空ID时不调用DAO")
    void testGetClassById_EmptyId_ShouldNotCallDAO() {
        // When
        Optional<Clazz> result = clazzService.getClassById("");
        
        // Then
        assertFalse(result.isPresent(), "空ID应该返回空结果");
        verify(clazzDAO, never()).getClassById(anyString());
    }
    
    @Test
    void testGetClassById_Exception_ShouldFallbackToGetAll() {
        // Given
        String classId = "C001";
        Clazz mockClazz = createMockClazz(classId, "测试班级");
        
        when(clazzDAO.getClassById(classId)).thenThrow(new RuntimeException("数据库错误"));
        when(clazzDAO.getAllClasses()).thenReturn(Arrays.asList(mockClazz));
        
        // When
        Optional<Clazz> result = clazzService.getClassById(classId);
        
        // Then
        assertTrue(result.isPresent(), "应该通过fallback机制找到班级");
        verify(clazzDAO, times(1)).getClassById(classId);
        verify(clazzDAO, times(1)).getAllClasses();
    }
    
    @Test
    @DisplayName("测试添加班级有效班级时调用DAO")
    void testAddClass_ValidClass_ShouldCallDAO() {
        // Given
        Clazz clazz = createMockClazz("C003", "新班级");
        when(clazzDAO.addClass(
            eq(clazz.getId()),
            eq(clazz.getName()),
            eq(clazz.getTeacherId()),
            eq(clazz.getDescription())
        )).thenReturn(1); // 添加这一行，模拟返回1表示添加成功
        
        // When
        assertDoesNotThrow(() -> clazzService.addClass(clazz));
        
        // Then
        verify(clazzDAO, times(1)).addClass(
            eq(clazz.getId()),
            eq(clazz.getName()),
            eq(clazz.getTeacherId()),
            eq(clazz.getDescription())
        );
    }
    
    @Test
    @DisplayName("测试添加班级空班级时抛出异常")
    void testAddClass_NullClass_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> clazzService.addClass(null)
        );
        assertEquals("班级信息不能为空", exception.getMessage());
        verify(clazzDAO, never()).addClass(anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("测试添加班级空名称时抛出异常")
    void testAddClass_EmptyName_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> clazzService.addClass(null)
        );
        assertEquals("班级信息不能为空", exception.getMessage());
        verify(clazzDAO, never()).addClass(anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("测试更新班级有效班级时调用DAO")
    void testUpdateClass_ValidClass_ShouldCallDAO() {
        // Given
        Clazz clazz = createMockClazz("C001", "更新班级");
        when(clazzDAO.updateClass(
            eq(clazz.getName()),
            eq(clazz.getTeacherId()),
            eq(clazz.getDescription()),
            eq(clazz.getId())
        )).thenReturn(true); // 添加这一行，模拟返回true表示更新成功
        
        // When
        assertDoesNotThrow(() -> clazzService.updateClass(clazz));
        
        // Then
        verify(clazzDAO, times(1)).updateClass(
            eq(clazz.getName()),
            eq(clazz.getTeacherId()),
            eq(clazz.getDescription()),
            eq(clazz.getId())
        );
    }
    
    @Test
    @DisplayName("测试删除班级有效ID时调用DAO")
    void testDeleteClass_ValidId_ShouldCallDAO() {
        // Given
        String classId = "C001";
        when(clazzDAO.deleteClass(classId)).thenReturn(true); // 添加这一行，模拟返回true表示删除成功
        
        // When
        assertDoesNotThrow(() -> clazzService.deleteClass(classId));
        
        // Then
        verify(clazzDAO, times(1)).deleteClass(classId);
    }
    
    @Test
    void testUpdateStudentCount_ValidParameters_ShouldCalculateAndUpdate() {
        // Given
        String classId = "C001";
        int increment = 1;
        Clazz mockClazz = createMockClazz(classId, "测试班级");
        mockClazz.setStudentCount(10);
        
        when(clazzDAO.getClassById(classId)).thenReturn(Optional.of(mockClazz));
        when(clazzDAO.updateStudentCount(eq(classId), anyInt())).thenReturn(true);
        
        // When
        assertDoesNotThrow(() -> clazzService.updateStudentCount(classId, increment));
        
        // Then
        verify(clazzDAO, times(1)).getClassById(classId);
        verify(clazzDAO, times(1)).updateStudentCount(classId, 11); // 10 + 1
    }
    
    @Test
    void testUpdateStudentCount_EmptyClassId_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> clazzService.updateStudentCount("", 1)
        );
        assertEquals("班级ID不能为空", exception.getMessage());
        verify(clazzDAO, never()).updateStudentCount(anyString(), anyInt());
    }
    
    @Test
    void testUpdateStudentCount_ClassNotFound_ShouldThrowException() {
        // Given
        String classId = "NONEXISTENT";
        when(clazzDAO.getClassById(classId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class, 
            () -> clazzService.updateStudentCount(classId, 1)
        );
        assertTrue(exception.getMessage().contains("班级不存在"));
        verify(clazzDAO, times(1)).getClassById(classId);
        verify(clazzDAO, never()).updateStudentCount(anyString(), anyInt());
    }
    
    @Test
    void testUpdateStudentCount_NegativeResult_ShouldSetToZero() {
        // Given
        String classId = "C001";
        int decrement = -5;
        Clazz mockClazz = createMockClazz(classId, "测试班级");
        mockClazz.setStudentCount(3);
        
        when(clazzDAO.getClassById(classId)).thenReturn(Optional.of(mockClazz));
        when(clazzDAO.updateStudentCount(eq(classId), anyInt())).thenReturn(true);
        
        // When
        assertDoesNotThrow(() -> clazzService.updateStudentCount(classId, decrement));
        
        // Then
        verify(clazzDAO, times(1)).updateStudentCount(classId, 0); // 不能为负数
    }
    
    @Test
    @DisplayName("测试根据教师获取班级时调用DAO")
    void testGetClassesByTeacher_ShouldCallDAO() {
        // Given
        String teacherId = "T001";
        List<Clazz> mockClasses = Arrays.asList(createMockClazz("C001", "班级1"));
        when(clazzDAO.getClassesByTeacher(teacherId)).thenReturn(mockClasses);
        
        // When
        List<Clazz> result = clazzService.getClassesByTeacher(teacherId);
        
        // Then
        assertNotNull(result, "结果不应为空");
        verify(clazzDAO, times(1)).getClassesByTeacher(teacherId);
    }
    
    private Clazz createMockClazz(String id, String name) {
        Clazz clazz = new Clazz();
        clazz.setId(id);
        clazz.setName(name);
        clazz.setTeacherId("T001");
        clazz.setStudentCount(0);
        clazz.setDescription("测试班级描述");
        return clazz;
    }
}