package site.arookieofc.transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.arookieofc.entity.Clazz;
import site.arookieofc.entity.Teacher;
import site.arookieofc.processor.config.ConfigProcessor;
import site.arookieofc.processor.transaction.TransactionInterceptor;
import site.arookieofc.processor.transaction.TransactionManager;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;
import site.arookieofc.service.impl.ClazzServiceImpl;
import site.arookieofc.service.impl.StudentServiceImpl;
import site.arookieofc.service.impl.TeacherServiceImpl;
import site.arookieofc.utils.DatabaseUtil;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    private TeacherService teacherService;
    private ClazzService clazzService;
    private StudentService studentService;
    
    // 测试数据
    private String teacherId;
    private String clazzId;
    
    @BeforeEach
    public void setUp() {
        // 确保配置被加载
        ConfigProcessor.injectStaticFields(DatabaseUtil.class);
        
        // 创建代理对象，以便事务生效
        teacherService = TransactionInterceptor.createProxy(new TeacherServiceImpl());
        clazzService = TransactionInterceptor.createProxy(new ClazzServiceImpl());
        studentService = TransactionInterceptor.createProxy(new StudentServiceImpl());
        
        // 生成较短的唯一ID用于测试
        String shortUuid = UUID.randomUUID().toString().substring(0, 6);
        teacherId = "t-" + shortUuid;  // 格式: t-a1b2c3
        clazzId = "c-" + shortUuid;    // 格式: c-a1b2c3
    }
    
    @AfterEach
    public void tearDown() {
        // 清理测试数据
        try {
            // 尝试删除测试班级
            Optional<Clazz> clazz = clazzService.getClassById(clazzId);
            if (clazz.isPresent()) {
                clazzService.deleteClass(clazzId);
            }
            
            // 尝试删除测试教师
            Optional<Teacher> teacher = teacherService.getTeacherById(teacherId);
            if (teacher.isPresent()) {
                teacherService.deleteTeacher(teacherId);
            }
        } catch (Exception e) {
            // 忽略清理过程中的异常
            System.err.println("清理测试数据时出错: " + e.getMessage());
        }
        
        // 清理事务状态
        TransactionManager.cleanup();
    }
    
    /**
     * 测试事务提交
     * 添加教师和班级，验证两者都成功添加
     */
    @Test
    public void testTransactionCommit() {
        // 创建测试数据
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setName("测试教师");
        teacher.setDepartment("测试部门");
        
        // 添加教师（有事务注解）
        teacherService.addTeacher(teacher);
        
        // 验证教师添加成功
        Optional<Teacher> savedTeacher = teacherService.getTeacherById(teacherId);
        assertTrue(savedTeacher.isPresent());
        assertEquals("测试教师", savedTeacher.get().getName());
        
        // 创建班级
        Clazz clazz = new Clazz();
        clazz.setId(clazzId);
        clazz.setName("测试班级");
        clazz.setTeacherId(teacherId);
        clazz.setDescription("事务测试班级");
        
        // 添加班级（有事务注解）
        clazzService.addClass(clazz);
        
        // 验证班级添加成功
        Optional<Clazz> savedClazz = clazzService.getClassById(clazzId);
        assertTrue(savedClazz.isPresent());
        assertEquals("测试班级", savedClazz.get().getName());
        assertEquals(teacherId, savedClazz.get().getTeacherId());
    }
    
    /**
     * 测试事务回滚
     * 尝试添加班级但指定不存在的教师ID，应该回滚
     */
    @Test
    public void testTransactionRollback() {
        // 创建一个指向不存在教师的班级
        Clazz clazz = new Clazz();
        clazz.setId(clazzId);
        clazz.setName("测试班级");
        clazz.setTeacherId("non-existent-teacher"); // 不存在的教师ID
        clazz.setDescription("事务测试班级");

        Throwable cause = assertThrows(Exception.class, () -> {
            clazzService.addClass(clazz);
        });
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        // 验证根本原因是IllegalArgumentException且包含预期消息
        assertInstanceOf(IllegalArgumentException.class, cause);
        assertTrue(cause.getMessage().contains("指定的班主任不存在"));
        // 验证班级未被添加（事务已回滚）
        Optional<Clazz> savedClazz = clazzService.getClassById(clazzId);
        assertFalse(savedClazz.isPresent());
    }

    @Test
    public void testTransactionPropagationRequired() {
        // 先添加教师
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setName("测试教师");
        teacher.setDepartment("测试部门");
        teacherService.addTeacher(teacher);
        
        // 验证教师添加成功
        assertTrue(teacherService.getTeacherById(teacherId).isPresent());
        
        // 创建班级并添加（使用REQUIRED传播行为）
        Clazz clazz = new Clazz();
        clazz.setId(clazzId);
        clazz.setName("测试班级");
        clazz.setTeacherId(teacherId);
        clazz.setDescription("事务测试班级");
        clazzService.addClass(clazz);
        
        // 验证班级添加成功
        Optional<Clazz> savedClazz = clazzService.getClassById(clazzId);
        assertTrue(savedClazz.isPresent());
    }

    @Test
    public void testNestedTransactions() {
        // 先添加教师
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setName("测试教师");
        teacher.setDepartment("测试部门");
        teacherService.addTeacher(teacher);
        
        // 验证教师添加成功
        assertTrue(teacherService.getTeacherById(teacherId).isPresent());
        
        try {
            // 尝试添加班级但故意使其失败
            Clazz clazz = new Clazz();
            clazz.setId(clazzId);
            clazz.setName("测试班级");
            clazz.setTeacherId("non-existent-teacher"); // 不存在的教师ID
            clazz.setDescription("事务测试班级");
            
            clazzService.addClass(clazz); // 这里会抛出异常
        } catch (Exception e) {
            // 预期会抛出异常，忽略
        }
        
        // 验证班级未添加成功（内部事务回滚）
        Optional<Clazz> savedClazz = clazzService.getClassById(clazzId);
        assertFalse(savedClazz.isPresent());
        
        // 但教师仍然存在（外部事务未回滚）
        Optional<Teacher> savedTeacher = teacherService.getTeacherById(teacherId);
        assertTrue(savedTeacher.isPresent());
    }
}