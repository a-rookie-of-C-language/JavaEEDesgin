package site.arookieofc.dao;

import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.entity.Student;

import java.util.List;
import java.util.Optional;

public interface StudentDAO {
    
    @SQL("SELECT * FROM student WHERE id = ?")
    Optional<Student> getStudentById(int id);
    
    @SQL(value = "INSERT INTO student (name, age, teacherId, clazz) VALUES (?, ?, ?, ?)", type = "INSERT")
    int addStudent(String name, int age, String teacherId, String clazz);
    
    @SQL(value = "UPDATE student SET name = ?, age = ?, teacherId = ?, clazz = ? WHERE id = ?", type = "UPDATE")
    boolean updateStudent(String name, int age, String teacherId, String clazz, int id);
    
    @SQL(value = "DELETE FROM student WHERE id = ?", type = "DELETE")
    boolean deleteStudent(int id);

    @SQL("SELECT * FROM student")
    List<Student> getAllStudents();
    
    @SQL("SELECT * FROM student WHERE clazz = ?")
    List<Student> getStudentsByClass(String clazz);
    
    @SQL("SELECT * FROM student WHERE teacherId = ?")
    List<Student> getStudentsByTeacherId(String teacherId);

    List<Student> getStudentsByTeacher(String teacherId);
}
