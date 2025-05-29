package site.arookieofc.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.arookieofc.entity.Teacher;
import site.arookieofc.pojo.dto.Result;
import site.arookieofc.service.TeacherService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherControllerTest {

    @Mock
    private TeacherService teacherService;
    
    private TeacherController teacherController;
    
    @BeforeEach
    void setUp() {
        teacherController = new TeacherController();
        setPrivateField(teacherController, "teacherService", teacherService);
    }
    
    @Test
    @DisplayName("测试获取所有教师成功时返回成功结果")
    void testGetAllTeachers_Success_ShouldReturnSuccessResult() {
        // Given
        List<Teacher> mockTeachers = Arrays.asList(
            createMockTeacher("T001", "张老师"),
            createMockTeacher("T002", "李老师")
        );
        when(teacherService.getAllTeachers()).thenReturn(mockTeachers);
        
        // When
        Result result = teacherController.getAllTeachers();
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        // 使用包含检查而不是精确匹配
        assertTrue(result.getMsg().contains("成功"), "消息应包含'成功'");
        assertNotNull(result.getData(), "数据不应为空");
        
        verify(teacherService, times(1)).getAllTeachers();
    }
    
    @Test
    @DisplayName("测试获取所有教师异常时返回错误结果")
    void testGetAllTeachers_Exception_ShouldReturnErrorResult() {
        // Given
        when(teacherService.getAllTeachers()).thenThrow(new RuntimeException("数据库错误"));
        
        // When
        Result result = teacherController.getAllTeachers();
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertFalse(result.isSuccess(), "应该返回失败结果");
        assertTrue(result.getMsg().contains("获取教师列表失败"));
        
        verify(teacherService, times(1)).getAllTeachers();
    }
    
    @Test
    @DisplayName("测试添加教师成功时返回成功结果")
    void testAddTeacher_Success_ShouldReturnSuccessResult() {
        // Given
        Teacher teacher = createMockTeacher("T003", "新老师");
        
        // When
        Result result = teacherController.addTeacher(teacher);
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals("添加教师成功", result.getMsg());
        
        verify(teacherService, times(1)).addTeacher(teacher);
    }
    
    @Test
    void testAddTeacher_Exception_ShouldReturnErrorResult() {
        // Given
        Teacher teacher = createMockTeacher("T003", "新老师");
        doThrow(new RuntimeException("添加失败")).when(teacherService).addTeacher(teacher);
        
        // When
        Result result = teacherController.addTeacher(teacher);
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertFalse(result.isSuccess(), "应该返回失败结果");
        assertTrue(result.getMsg().contains("添加教师失败"));
        
        verify(teacherService, times(1)).addTeacher(teacher);
    }
    
    @Test
    @DisplayName("测试更新教师成功时设置ID并返回成功结果")
    void testUpdateTeacher_Success_ShouldSetIdAndReturnSuccess() {
        // Given
        String teacherId = "T001";
        Teacher teacher = createMockTeacher(null, "更新老师");
        
        // When
        Result result = teacherController.updateTeacher(teacherId, teacher);
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals("更新教师成功", result.getMsg());
        assertEquals(teacherId, teacher.getId(), "应该设置教师ID");
        
        verify(teacherService, times(1)).updateTeacher(teacher);
    }
    
    @Test
    @DisplayName("测试删除教师成功时返回成功结果")
    void testDeleteTeacher_Success_ShouldReturnSuccessResult() {
        // Given
        String teacherId = "T001";
        
        // When
        Result result = teacherController.deleteTeacher(teacherId);
        
        // Then
        assertNotNull(result, "结果不应为空");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        assertEquals("删除教师成功", result.getMsg());
        
        verify(teacherService, times(1)).deleteTeacher(teacherId);
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