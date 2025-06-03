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

    @SQL(value = "INSERT INTO teacher (id, name) VALUES (?, ?)", type = "INSERT")
    int addTeacher(String id, String name);
    
    @SQL(value = "UPDATE teacher SET name = ? WHERE id = ?", type = "UPDATE")
    boolean updateTeacher(String name,String id);
    
    @SQL(value = "DELETE FROM teacher WHERE id = ?", type = "DELETE")
    boolean deleteTeacher(String id);
}