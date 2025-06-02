package site.arookieofc.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.arookieofc.entity.Teacher;
import site.arookieofc.processor.config.ConfigProcessor;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.utils.DatabaseUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TeacherDAOTest {
    
    private TeacherDAO teacherDAO;
    private String testTeacherId;

    static {
        ConfigProcessor.injectStaticFields(DatabaseUtil.class);
    }
    
    @BeforeEach
    public void setUp() {
        // 获取DAO实例
        teacherDAO = DAOFactory.getDAO(TeacherDAO.class);
        // 生成唯一ID用于测试
        testTeacherId = "TEST-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    @Test
    public void testAddAndGetTeacher() {
        // 添加测试数据
        int result = teacherDAO.addTeacher(testTeacherId, "测试教师", "测试部门");
        assertTrue(result > 0, "添加教师应该成功");
        
        // 通过ID获取教师
        Optional<Teacher> teacher = teacherDAO.getTeacherById(testTeacherId);
        assertTrue(teacher.isPresent(), "应该能通过ID找到教师");
        assertEquals("测试教师", teacher.get().getName(), "教师姓名应该匹配");
        assertEquals("测试部门", teacher.get().getDepartment(), "教师部门应该匹配");
        
        // 测试完成后删除测试数据
        boolean deleted = teacherDAO.deleteTeacher(testTeacherId);
        assertTrue(deleted, "删除教师应该成功");
        
        // 验证删除成功
        Optional<Teacher> deletedTeacher = teacherDAO.getTeacherById(testTeacherId);
        assertFalse(deletedTeacher.isPresent(), "删除后应该找不到该教师");
    }
    
    @Test
    public void testUpdateTeacher() {
        // 添加测试数据
        int result = teacherDAO.addTeacher(testTeacherId, "更新测试", "测试部门");
        assertTrue(result > 0, "添加教师应该成功");
        
        // 更新教师信息
        boolean updated = teacherDAO.updateTeacher("更新后的名字", "更新后的部门",testTeacherId);
        assertTrue(updated, "更新教师应该成功");
        
        // 验证更新成功
        Optional<Teacher> updatedTeacher = teacherDAO.getTeacherById(testTeacherId);
        assertTrue(updatedTeacher.isPresent(), "应该能找到更新后的教师");
        assertEquals("更新后的名字", updatedTeacher.get().getName(), "教师姓名应该已更新");
        assertEquals("更新后的部门", updatedTeacher.get().getDepartment(), "教师部门应该已更新");
        
        // 测试完成后删除测试数据
        boolean deleted = teacherDAO.deleteTeacher(testTeacherId);
        assertTrue(deleted, "删除教师应该成功");
    }
    
    @Test
    public void testGetAllTeachers() {
        // 记录初始教师数量
        List<Teacher> initialTeachers = teacherDAO.getAllTeachers();
        int initialCount = initialTeachers.size();
        
        // 添加测试数据
        String testId1 = testTeacherId + "-1";
        String testId2 = testTeacherId + "-2";
        
        teacherDAO.addTeacher(testId1, "测试教师1", "测试部门1");
        teacherDAO.addTeacher(testId2, "测试教师2", "测试部门2");
        
        // 验证教师数量增加了2
        List<Teacher> updatedTeachers = teacherDAO.getAllTeachers();
        assertEquals(initialCount + 2, updatedTeachers.size(), "教师总数应该增加2");
        
        // 测试完成后删除测试数据
        teacherDAO.deleteTeacher(testId1);
        teacherDAO.deleteTeacher(testId2);
        
        // 验证删除成功
        List<Teacher> finalTeachers = teacherDAO.getAllTeachers();
        assertEquals(initialCount, finalTeachers.size(), "删除后教师总数应该恢复");
    }
    
    @AfterEach
    public void tearDown() {
        // 确保测试数据被清理
        try {
            Optional<Teacher> teacher = teacherDAO.getTeacherById(testTeacherId);
            if (teacher.isPresent()) {
                teacherDAO.deleteTeacher(testTeacherId);
            }
            
            // 清理所有测试ID相关的数据
            if (testTeacherId != null) {
                teacherDAO.deleteTeacher(testTeacherId + "-1");
                teacherDAO.deleteTeacher(testTeacherId + "-2");
            }
            
            // 清理所有测试数据
            List<Teacher> testTeachers = teacherDAO.getAllTeachers().stream()
                    .filter(t -> t.getId() != null && t.getId().startsWith("TEST-"))
                    .toList();
            
            for (Teacher t : testTeachers) {
                teacherDAO.deleteTeacher(t.getId());
            }
        } catch (Exception e) {
            // 忽略清理过程中的异常
            System.err.println("清理测试数据时出错: " + e.getMessage());
        }
    }
}