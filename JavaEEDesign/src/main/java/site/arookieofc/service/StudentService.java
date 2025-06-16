package site.arookieofc.service;

import site.arookieofc.pojo.DO.Student;
import site.arookieofc.pojo.dto.PageResult;
import java.util.List;
import java.util.Optional;

public interface StudentService {

    Student getStudentById(String id);

    void addStudent(Student student);

    void updateStudent(Student student);

    void deleteStudent(String id);

    List<Student> getAllStudents();

    PageResult<Student> getStudentsByPage(int page, int size);

    long getTotalStudentCount();

    List<Student> getStudentsByClass(String clazz);

    List<Student> getStudentsByTeacher(String teacherId);

    List<Student> getStudentsByTeacherId(String teacherId);

    // 添加批量操作方法
    void addStudentsBatch(List<Student> students);

    void deleteStudentsBatch(List<String> ids);
}
