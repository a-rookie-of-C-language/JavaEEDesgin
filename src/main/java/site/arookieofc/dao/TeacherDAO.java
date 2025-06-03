package site.arookieofc.dao;

import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.entity.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherDAO {
    
    @SQL("SELECT * FROM teacher")
    List<Teacher> getAllTeachers();
    
    @SQL("SELECT * FROM teacher WHERE id = ?")
    Optional<Teacher> getTeacherById(String id);

    @SQL("SELECT DISTINCT clazz FROM student WHERE clazz IS NOT NULL AND clazz != ''")
    List<String> getAllClassNames();

    @SQL(value = "INSERT INTO teacher (id, name, department) VALUES (?, ?, ?)", type = "INSERT")
    int addTeacher(String id, String name, String department);
    
    @SQL(value = "UPDATE teacher SET name = ?, department = ? WHERE id = ?", type = "UPDATE")
    boolean updateTeacher(String name, String department,String id);
    
    @SQL(value = "DELETE FROM teacher WHERE id = ?", type = "DELETE")
    boolean deleteTeacher(String id);
}