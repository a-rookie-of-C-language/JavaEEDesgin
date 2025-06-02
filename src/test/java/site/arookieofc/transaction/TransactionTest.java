package site.arookieofc.transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.arookieofc.entity.Teacher;
import site.arookieofc.processor.config.ConfigProcessor;
import site.arookieofc.processor.transaction.TransactionInterceptor;
import site.arookieofc.service.TeacherService;
import site.arookieofc.service.impl.TeacherServiceImpl;
import site.arookieofc.dao.TeacherDAO;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.utils.DatabaseUtil;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事务功能测试类
 * 测试自定义事务注解和事务管理器是否正常工作
 */
public class TransactionTest {

    private TeacherService teacherService;
    private TeacherDAO teacherDAO;
    private String testTeacherId;

    static {
        ConfigProcessor.injectStaticFields(DatabaseUtil.class);
    }

    @BeforeEach
    public void setUp() {
        teacherService = TransactionInterceptor.createProxy(new TeacherServiceImpl());
        teacherDAO = DAOFactory.getDAO(TeacherDAO.class);
        testTeacherId = "T-TRANS-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    public void testTransactionCommit() {
        Teacher teacher = new Teacher();
        teacher.setId(testTeacherId);
        teacher.setName("事务测试教师");
        teacher.setDepartment("事务测试部门");
        teacherService.addTeacher(teacher);
        Optional<Teacher> savedTeacher = teacherDAO.getTeacherById(testTeacherId);
        assertTrue(savedTeacher.isPresent(), "事务提交后，教师应该存在于数据库中");
        assertEquals("事务测试教师", savedTeacher.get().getName(), "教师名称应该匹配");
    }

    /**
     * 测试事务回滚
     * 当发生异常时，事务应该回滚，数据库中不应该有新增的记录
     */
    @Test
    public void testTransactionRollback() {
        Teacher teacher = new Teacher();
        teacher.setId(testTeacherId);
        teacher.setName("事务回滚测试");
        teacher.setDepartment("事务测试部门");

        try {
            teacherService.addTeacherThrowable(teacher);
        } catch (Throwable e) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                // 忽略中断异常
            }
            // 验证数据是否已回滚
            Optional<Teacher> savedTeacher = teacherDAO.getTeacherById(testTeacherId);
            System.out.println("教师是否存在: " + savedTeacher.isPresent());
            assertFalse(savedTeacher.isPresent(), "事务回滚后，教师不应该存在于数据库中");
        }
    }

    /**
     * 测试嵌套事务
     * 测试REQUIRED传播行为是否正常工作
     */
    @Test
    public void testNestedTransaction() {
        // 创建一个扩展的服务，模拟嵌套事务场景
        TeacherService nestedService = TransactionInterceptor.createProxy(new TeacherServiceImpl() {
            @Override
            public void addTeacher(Teacher teacher) {
                // 先添加教师
                super.addTeacher(teacher);

                // 然后更新教师（在同一个事务中）
                teacher.setName(teacher.getName() + "-已更新");
                updateTeacher(teacher);
            }
        });

        // 创建测试教师对象
        Teacher teacher = new Teacher();
        teacher.setId(testTeacherId);
        teacher.setName("嵌套事务测试");
        teacher.setDepartment("事务测试部门");

        // 执行嵌套事务操作
        nestedService.addTeacher(teacher);

        // 验证最终结果
        Optional<Teacher> savedTeacher = teacherDAO.getTeacherById(testTeacherId);
        assertTrue(savedTeacher.isPresent(), "嵌套事务提交后，教师应该存在于数据库中");
        assertEquals("嵌套事务测试-已更新", savedTeacher.get().getName(), "教师名称应该是更新后的值");
    }

    @AfterEach
    public void tearDown() {
        // 清理测试数据
        try {
            teacherDAO.deleteTeacher(testTeacherId);
        } catch (Exception e) {
            System.err.println("清理测试数据时出错: " + e.getMessage());
        }
    }
}