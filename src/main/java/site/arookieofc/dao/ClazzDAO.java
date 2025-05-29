package site.arookieofc.dao;

import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.entity.Clazz;

import java.util.List;
import java.util.Optional;

public interface ClazzDAO {
    
    @SQL("SELECT * FROM class")
    List<Clazz> getAllClasses();
    
    @SQL("SELECT * FROM class WHERE id = ?")
    Optional<Clazz> getClassById(String id);
    
    @SQL(value = "INSERT INTO class (id, name, teacherId, description) VALUES (?, ?, ?, ?)", type = "INSERT")
    int addClass(String id, String name, String teacherId, String description);
    
    @SQL(value = "UPDATE class SET name = ?, teacherId = ?, description = ? WHERE id = ?", type = "UPDATE")
    boolean updateClass(String name, String teacherId, String description, String id);
    
    @SQL(value = "DELETE FROM class WHERE id = ?", type = "DELETE")
    boolean deleteClass(String id);
    
    @SQL("SELECT * FROM class WHERE teacherId = ?")
    List<Clazz> getClassesByTeacher(String teacherId);

    @SQL(value = "UPDATE class SET studentCount = ? WHERE id = ?", type = "UPDATE")
    boolean updateStudentCount(String classId, int newCount);
}