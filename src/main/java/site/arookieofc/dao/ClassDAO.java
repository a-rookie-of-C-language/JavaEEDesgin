package site.arookieofc.dao;

import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.entity.Class;

import java.util.List;
import java.util.Optional;

public interface ClassDAO {
    
    @SQL("SELECT * FROM class")
    List<Class> getAllClasses();
    
    @SQL("SELECT * FROM class WHERE id = ?")
    Optional<Class> getClassById(String id);
    
    @SQL(value = "INSERT INTO class (id, name, teacherId, description) VALUES (?, ?, ?, ?)", type = "INSERT")
    int addClass(String id, String name, String teacherId, String description);
    
    @SQL(value = "UPDATE class SET name = ?, teacherId = ?, description = ? WHERE id = ?", type = "UPDATE")
    boolean updateClass(String name, String teacherId, String description, String id);
    
    @SQL(value = "DELETE FROM class WHERE id = ?", type = "DELETE")
    boolean deleteClass(String id);
    
    @SQL("SELECT * FROM class WHERE teacherId = ?")
    List<Class> getClassesByTeacher(String teacherId);
}