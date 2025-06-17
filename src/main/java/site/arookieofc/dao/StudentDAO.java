package site.arookieofc.dao;

import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.pojo.DO.Student;
import java.util.List;
import java.util.Optional;

public interface StudentDAO {

    @SQL("SELECT * FROM student WHERE id = ?")
    Optional<Student> getStudentById(String id);

    @SQL(value = "INSERT INTO student (id,name, age, teacher_id, clazz_id) VALUES (?,?, ?, ?, ?)", type = "INSERT")
    int addStudent(String id, String name, int age, String teacherId, String clazz);

    @SQL(value = "UPDATE student SET name = ?, age = ?, teacher_id = ?, clazz_id = ? WHERE id = ?", type = "UPDATE")
    boolean updateStudent(String name, int age, String teacherId, String clazz, String id);

    @SQL(value = "DELETE FROM student WHERE id = ?", type = "DELETE")
    boolean deleteStudent(String  id);

    @SQL("SELECT * FROM student")
    Optional<List<Student>> getAllStudents();

    @SQL("SELECT * FROM student WHERE clazz_id = ?")
    Optional<List<Student>> getStudentsByClass(String clazz);

    @SQL("SELECT * FROM student WHERE teacher_id = ?")
    Optional<List<Student>> getStudentsByTeacherId(String teacherId);

    @SQL("SELECT * FROM student WHERE teacher_id = ?")
    Optional<List<Student>> getStudentsByTeacher(String teacherId);

    @SQL("SELECT * FROM student LIMIT ? OFFSET ?")
    Optional<List<Student>> getStudentsByPage(int limit, int offset);

    @SQL("SELECT COUNT(*) FROM student")
    long getTotalStudentCount();

    @SQL(value = "UPDATE student SET teacher_id = ? WHERE clazz_id = ?", type = "UPDATE")
    boolean updateStudentsTeacherByClass(String teacherId, String clazzId);
}
