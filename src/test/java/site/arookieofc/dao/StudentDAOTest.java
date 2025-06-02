package site.arookieofc.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.arookieofc.entity.Student;
import site.arookieofc.processor.config.ConfigProcessor;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.utils.DatabaseUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StudentDAOTest {

    static {
        ConfigProcessor.injectStaticFields(DatabaseUtil.class);
    }
    
    private StudentDAO studentDAO;
    private String testStudentId;
    
    @BeforeEach
    public void setUp() {
        // 获取DAO实例
        studentDAO = DAOFactory.getDAO(StudentDAO.class);
    }
    
    @Test
    public void testAddAndGetStudent() {
        // 添加测试数据
        int result = studentDAO.addStudent(UUID.randomUUID().toString(),"测试学生", 20, "T001", "测试班级");
        assertTrue(result > 0, "添加学生应该成功");
        
        // 获取刚刚插入的学生ID
        List<Student> allStudents = studentDAO.getAllStudents();
        Optional<Student> addedStudent = allStudents.stream()
                .filter(s -> "测试学生".equals(s.getName()))
                .findFirst();
        
        assertTrue(addedStudent.isPresent(), "应该能找到刚添加的学生");
        testStudentId = addedStudent.get().getId();
        
        // 通过ID获取学生
        Optional<Student> student = studentDAO.getStudentById(testStudentId);
        assertTrue(student.isPresent(), "应该能通过ID找到学生");
        assertEquals("测试学生", student.get().getName(), "学生姓名应该匹配");
        assertEquals(20, student.get().getAge(), "学生年龄应该匹配");
        assertEquals("T001", student.get().getTeacherId(), "学生教师ID应该匹配");
        assertEquals("测试班级", student.get().getClazz(), "学生班级应该匹配");
        
        // 测试完成后删除测试数据
        boolean deleted = studentDAO.deleteStudent(testStudentId);
        assertTrue(deleted, "删除学生应该成功");
        
        // 验证删除成功
        Optional<Student> deletedStudent = studentDAO.getStudentById(testStudentId);
        assertFalse(deletedStudent.isPresent(), "删除后应该找不到该学生");
    }
    
    @Test
    public void testUpdateStudent() {
        // 添加测试数据
        int result = studentDAO.addStudent(UUID.randomUUID().toString(),"更新测试", 18, "T002", "测试班级2");
        assertTrue(result > 0, "添加学生应该成功");
        
        // 获取刚刚插入的学生ID
        List<Student> allStudents = studentDAO.getAllStudents();
        Optional<Student> addedStudent = allStudents.stream()
                .filter(s -> "更新测试".equals(s.getName()))
                .findFirst();
        
        assertTrue(addedStudent.isPresent(), "应该能找到刚添加的学生");
        testStudentId = addedStudent.get().getId();
        
        // 更新学生信息
        boolean updated = studentDAO.updateStudent("更新后的名字", 19, "T003", "更新后的班级", testStudentId);
        assertTrue(updated, "更新学生应该成功");
        
        // 验证更新成功
        Optional<Student> updatedStudent = studentDAO.getStudentById(testStudentId);
        assertTrue(updatedStudent.isPresent(), "应该能找到更新后的学生");
        assertEquals("更新后的名字", updatedStudent.get().getName(), "学生姓名应该已更新");
        assertEquals(19, updatedStudent.get().getAge(), "学生年龄应该已更新");
        assertEquals("T003", updatedStudent.get().getTeacherId(), "学生教师ID应该已更新");
        assertEquals("更新后的班级", updatedStudent.get().getClazz(), "学生班级应该已更新");
        
        // 测试完成后删除测试数据
        boolean deleted = studentDAO.deleteStudent(testStudentId);
        assertTrue(deleted, "删除学生应该成功");
    }
    
    @Test
    public void testGetStudentsByClass() {
        // 添加测试数据
        int result1 = studentDAO.addStudent(UUID.randomUUID().toString(),"班级测试1", 18, "T001", "测试班级A");
        int result2 = studentDAO.addStudent(UUID.randomUUID().toString(),"班级测试2", 19, "T002", "测试班级A");
        int result3 = studentDAO.addStudent(UUID.randomUUID().toString(),"班级测试3", 20, "T003", "测试班级B");
        
        assertTrue(result1 > 0 && result2 > 0 && result3 > 0, "添加学生应该成功");
        
        // 获取刚刚插入的学生ID
        List<Student> allStudents = studentDAO.getAllStudents();
        List<String> testStudentIds = allStudents.stream()
                .filter(s -> s.getName().startsWith("班级测试"))
                .map(Student::getId)
                .toList();
        
        assertEquals(3, testStudentIds.size(), "应该添加了3个测试学生");
        
        // 测试按班级查询
        List<Student> classAStudents = studentDAO.getStudentsByClass("测试班级A");
        assertEquals(2, classAStudents.size(), "测试班级A应该有2个学生");
        
        List<Student> classBStudents = studentDAO.getStudentsByClass("测试班级B");
        assertEquals(1, classBStudents.size(), "测试班级B应该有1个学生");
        
        // 测试完成后删除测试数据
        for (String id : testStudentIds) {
            studentDAO.deleteStudent(id);
        }
        
        // 验证删除成功
        List<Student> remainingStudents = studentDAO.getStudentsByClass("测试班级A");
        assertEquals(0, remainingStudents.size(), "删除后应该没有测试班级A的学生");
        
        remainingStudents = studentDAO.getStudentsByClass("测试班级B");
        assertEquals(0, remainingStudents.size(), "删除后应该没有测试班级B的学生");
    }
    
    @AfterEach
    public void tearDown() {
        // 确保测试数据被清理
        try {
            Optional<Student> student = studentDAO.getStudentById(testStudentId);
            if (student.isPresent()) {
                studentDAO.deleteStudent(testStudentId);
            }
            
            // 清理所有测试数据
            List<Student> testStudents = studentDAO.getAllStudents().stream()
                    .filter(s -> s.getName() != null && 
                           (s.getName().startsWith("测试") || 
                            s.getName().startsWith("更新") || 
                            s.getName().startsWith("班级测试")))
                    .toList();
            
            for (Student s : testStudents) {
                studentDAO.deleteStudent(s.getId());
            }
        } catch (Exception e) {
            // 忽略清理过程中的异常
            System.err.println("清理测试数据时出错: " + e.getMessage());
        }
    }
}