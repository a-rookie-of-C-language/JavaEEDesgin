package site.arookieofc.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.arookieofc.entity.Clazz;
import site.arookieofc.pojo.dto.Result;
import site.arookieofc.service.ClazzService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClazzControllerTest {

    @Mock
    private ClazzService clazzService;
    
    private ClazzController clazzController;
    
    @BeforeEach
    void setUp() {
        clazzController = new ClazzController();
        setPrivateField(clazzController, "clazzService", clazzService);
    }
    
    @Test
    @DisplayName("测试获取所有班级列表成功时返回成功结果")
    void testGetAllClasses_Success_ShouldReturnSuccessResult() {
        // Given
        List<Clazz> mockClasses = Arrays.asList(
            createMockClazz("C001", "班级1"),
            createMockClazz("C002", "班级2")
        );
        when(clazzService.getAllClasses()).thenReturn(mockClasses);
        
        // When
        Result result = clazzController.getAllClasses();
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertTrue(result.getMsg().contains("成功"), "消息应包含'成功'");
        assertNotNull(result.getData(), "数据不应为空");
        
        verify(clazzService, times(1)).getAllClasses();
    }
    
    @Test
    @DisplayName("测试获取所有班级列表异常时返回错误结果")
    void testGetAllClasses_Exception_ShouldReturnErrorResult() {
        // Given
        when(clazzService.getAllClasses()).thenThrow(new RuntimeException("数据库错误"));
        
        // When
        Result result = clazzController.getAllClasses();
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertFalse(result.isSuccess(), "应该返回失败结果");
        assertTrue(result.getMsg().contains("获取班级列表失败"));
        
        verify(clazzService, times(1)).getAllClasses();
    }
    
    @Test
    @DisplayName("测试添加班级成功时返回成功结果")
    void testAddClass_Success_ShouldReturnSuccessResult() {
        // Given
        Clazz clazz = createMockClazz("C003", "新班级");
        
        // When
        Result result = clazzController.addClass(clazz);
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals("添加班级成功", result.getMsg());
        
        verify(clazzService, times(1)).addClass(clazz);
    }
    
    @Test
    @DisplayName("测试添加班级异常时返回错误结果")
    void testAddClass_Exception_ShouldReturnErrorResult() {
        // Given
        Clazz clazz = createMockClazz("C003", "新班级");
        doThrow(new RuntimeException("添加失败")).when(clazzService).addClass(clazz);
        
        // When
        Result result = clazzController.addClass(clazz);
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertFalse(result.isSuccess(), "应该返回失败结果");
        assertTrue(result.getMsg().contains("添加班级失败"));
        
        verify(clazzService, times(1)).addClass(clazz);
    }
    
    @Test
    @DisplayName("测试更新班级成功时设置ID并返回成功结果")
    void testUpdateClass_Success_ShouldSetIdAndReturnSuccess() {
        // Given
        String classId = "C001";
        Clazz clazz = createMockClazz(null, "更新班级");
        
        // When
        Result result = clazzController.updateClass(classId, clazz);
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals("更新班级成功", result.getMsg());
        assertEquals(classId, clazz.getId(), "应该设置班级ID");
        
        verify(clazzService, times(1)).updateClass(clazz);
    }
    
    @Test
    @DisplayName("测试删除班级成功时返回成功结果")
    void testDeleteClass_Success_ShouldReturnSuccessResult() {
        // Given
        String classId = "C001";
        
        // When
        Result result = clazzController.deleteClass(classId);
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals("删除班级成功", result.getMsg());
        
        verify(clazzService, times(1)).deleteClass(classId);
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