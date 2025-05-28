package site.arookieofc.service;

import site.arookieofc.entity.Student;
import site.arookieofc.pojo.dto.PageResult;

import java.util.List;
import java.util.Optional;

public interface StudentService {
    
    /**
     * 根据ID获取学生信息
     */
    Optional<Student> getStudentById(int id);
    
    /**
     * 添加学生
     */
    void addStudent(Student student);
    
    /**
     * 更新学生信息
     */
    void updateStudent(Student student);
    
    /**
     * 删除学生
     */
    void deleteStudent(int id);
    
    /**
     * 获取所有学生
     */
    List<Student> getAllStudents();
    
    /**
     * 分页获取学生列表
     */
    PageResult<Student> getStudentsByPage(int page, int size);
    
    /**
     * 获取学生总数
     */
    long getTotalStudentCount();
    
    /**
     * 根据班级获取学生
     */
    List<Student> getStudentsByClass(String clazz);
    
    /**
     * 根据教师ID获取学生
     */
    List<Student> getStudentsByTeacher(String teacherId);

    List<Student> getStudentsByTeacherId(String teacherId);
}
