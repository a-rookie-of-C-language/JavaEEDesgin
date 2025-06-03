package site.arookieofc.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.entity.Clazz;
import site.arookieofc.processor.config.ConfigProcessor;
import site.arookieofc.processor.ioc.AnnotationApplicationContext;
import site.arookieofc.processor.ioc.ApplicationContextHolder;
import site.arookieofc.utils.DatabaseUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ClazzDAOTest {

    @Autowired
    private ClazzDAO clazzDAO;
    @Autowired
    private TeacherDAO teacherDAO;
    private String testClassId;
    private String testTeacherId;
    static {
        ConfigProcessor.injectStaticFields(DatabaseUtil.class);
        AnnotationApplicationContext applicationContext;
        try {
            applicationContext = new AnnotationApplicationContext();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ApplicationContextHolder.setApplicationContext(applicationContext);
    }
    
    @BeforeEach
    public void setUp() {
        
        // 生成唯一ID用于测试
        testClassId = "C-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        testTeacherId = "T-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        
        // 添加测试教师
        teacherDAO.addTeacher(testTeacherId, "测试班主任", "测试部门");
    }
    
    @Test
    public void testAddAndGetClass() {
        // 添加测试数据
        int result = clazzDAO.addClass(testClassId, "测试班级", testTeacherId, "测试班级描述");
        assertTrue(result > 0, "添加班级应该成功");
        
        // 通过ID获取班级
        Optional<Clazz> clazz = clazzDAO.getClassById(testClassId);
        assertTrue(clazz.isPresent(), "应该能通过ID找到班级");
        assertEquals("测试班级", clazz.get().getName(), "班级名称应该匹配");
        assertEquals(testTeacherId, clazz.get().getTeacherId(), "班主任ID应该匹配");
        assertEquals("测试班级描述", clazz.get().getDescription(), "班级描述应该匹配");
        
        // 测试完成后删除测试数据
        boolean deleted = clazzDAO.deleteClass(testClassId);
        assertTrue(deleted, "删除班级应该成功");
        
        // 验证删除成功
        Optional<Clazz> deletedClass = clazzDAO.getClassById(testClassId);
        assertFalse(deletedClass.isPresent(), "删除后应该找不到该班级");
    }
    
    @Test
    public void testUpdateClass() {
        // 添加测试数据
        int result = clazzDAO.addClass(testClassId, "更新测试", testTeacherId, "测试描述");
        assertTrue(result > 0, "添加班级应该成功");
        
        // 更新班级信息
        boolean updated = clazzDAO.updateClass("更新后的名字", testTeacherId, "更新后的描述", testClassId);
        assertTrue(updated, "更新班级应该成功");
        
        // 验证更新成功
        Optional<Clazz> updatedClass = clazzDAO.getClassById(testClassId);
        assertTrue(updatedClass.isPresent(), "应该能找到更新后的班级");
        assertEquals("更新后的名字", updatedClass.get().getName(), "班级名称应该已更新");
        assertEquals("更新后的描述", updatedClass.get().getDescription(), "班级描述应该已更新");
        
        // 测试完成后删除测试数据
        boolean deleted = clazzDAO.deleteClass(testClassId);
        assertTrue(deleted, "删除班级应该成功");
    }
    
    @Test
    public void testUpdateStudentCount() {
        // 添加测试数据
        int result = clazzDAO.addClass(testClassId, "学生数量测试", testTeacherId, "测试描述");
        assertTrue(result > 0, "添加班级应该成功");
        
        // 更新学生数量
        boolean updated = clazzDAO.updateStudentCount(30, testClassId);
        assertTrue(updated, "更新学生数量应该成功");
        
        // 验证更新成功
        Optional<Clazz> updatedClass = clazzDAO.getClassById(testClassId);
        assertTrue(updatedClass.isPresent(), "应该能找到更新后的班级");
        assertEquals(30, updatedClass.get().getStudentCount(), "学生数量应该已更新");
        
        // 测试完成后删除测试数据
        boolean deleted = clazzDAO.deleteClass(testClassId);
        assertTrue(deleted, "删除班级应该成功");
    }
    
    @Test
    public void testGetClassesByTeacher() {
        // 添加测试数据
        String testClassId1 = testClassId + "-1";
        String testClassId2 = testClassId + "-2";
        
        clazzDAO.addClass(testClassId1, "测试班级1", testTeacherId, "测试描述1");
        clazzDAO.addClass(testClassId2, "测试班级2", testTeacherId, "测试描述2");
        
        // 测试按教师ID查询
        List<Clazz> teacherClasses = clazzDAO.getClassesByTeacher(testTeacherId);
        assertEquals(2, teacherClasses.size(), "该教师应该有2个班级");
        
        // 测试完成后删除测试数据
        clazzDAO.deleteClass(testClassId1);
        clazzDAO.deleteClass(testClassId2);
        
        // 验证删除成功
        List<Clazz> remainingClasses = clazzDAO.getClassesByTeacher(testTeacherId);
        assertEquals(0, remainingClasses.size(), "删除后该教师应该没有班级");
    }
    
    @AfterEach
    public void tearDown() {
        // 确保测试数据被清理
        try {
            // 清理班级数据
            Optional<Clazz> clazz = clazzDAO.getClassById(testClassId);
            if (clazz.isPresent()) {
                clazzDAO.deleteClass(testClassId);
            }
            
            // 清理所有测试ID相关的班级数据
            if (testClassId != null) {
                clazzDAO.deleteClass(testClassId + "-1");
                clazzDAO.deleteClass(testClassId + "-2");
            }
            
            // 清理所有测试班级数据
            List<Clazz> testClasses = clazzDAO.getAllClasses().stream()
                    .filter(c -> c.getId() != null && c.getId().startsWith("C-TEST-"))
                    .toList();
            
            for (Clazz c : testClasses) {
                clazzDAO.deleteClass(c.getId());
            }
            
            // 清理教师数据
            teacherDAO.deleteTeacher(testTeacherId);
        } catch (Exception e) {
            // 忽略清理过程中的异常
            System.err.println("清理测试数据时出错: " + e.getMessage());
        }
    }
}