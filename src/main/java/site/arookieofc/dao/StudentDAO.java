package site.arookieofc.dao;

import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.entity.Student;

import java.util.List;
import java.util.Optional;

public interface StudentDAO {

    @SQL("SELECT * FROM student WHERE id = ?")
    Optional<Student> getStudentById(String id);

    @SQL(value = "INSERT INTO student (id,name, age, teacherId, clazz) VALUES (?,?, ?, ?, ?)", type = "INSERT")
    int addStudent(String id, String name, int age, String teacherId, String clazz);

    @SQL(value = "UPDATE student SET name = ?, age = ?, teacherId = ?, clazz = ? WHERE id = ?", type = "UPDATE")
    boolean updateStudent(String name, int age, String teacherId, String clazz, String id);

    @SQL(value = "DELETE FROM student WHERE id = ?", type = "DELETE")
    boolean deleteStudent(String  id);

    @SQL("SELECT * FROM student")
    List<Student> getAllStudents();

    @SQL("SELECT * FROM student WHERE clazz = ?")
    List<Student> getStudentsByClass(String clazz);

    @SQL("SELECT * FROM student WHERE teacherId = ?")
    List<Student> getStudentsByTeacherId(String teacherId);

    @SQL("SELECT * FROM student WHERE teacherId = ?")
    List<Student> getStudentsByTeacher(String teacherId);

    @SQL("SELECT * FROM student LIMIT ? OFFSET ?")
    List<Student> getStudentsByPage(int limit, int offset);

    @SQL("SELECT COUNT(*) FROM student")
    long getTotalStudentCount();
}
