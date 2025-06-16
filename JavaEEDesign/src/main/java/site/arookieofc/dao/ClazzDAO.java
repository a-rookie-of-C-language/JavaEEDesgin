package site.arookieofc.dao;

import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.pojo.DO.Clazz;
import java.util.List;
import java.util.Optional;

public interface ClazzDAO {

    @SQL("SELECT * FROM class")
    Optional<List<Clazz>> getAllClasses();

    @SQL("SELECT * FROM class WHERE id = ?")
    Optional<Clazz> getClassById(String id);

    @SQL(value = "INSERT INTO class (id, name, teacherId) VALUES (?, ?, ?)", type = "INSERT")
    int addClass(String id, String name, String teacherId);

    @SQL(value = "UPDATE class SET name = ?, teacherId = ? WHERE id = ?", type = "UPDATE")
    boolean updateClass(String name, String teacherId, String id);

    @SQL(value = "DELETE FROM class WHERE id = ?", type = "DELETE")
    boolean deleteClass(String id);

    @SQL("SELECT * FROM class WHERE teacherId = ?")
    Optional<List<Clazz>> getClassesByTeacher(String teacherId);

    @SQL(value = "UPDATE class SET student_count = ? WHERE id = ?", type = "UPDATE")
    boolean updateStudentCount(int newCount, String id);

    @SQL("SELECT id FROM class WHERE name = ?")
    Optional<String> getClassIdByName(String clazz);
}