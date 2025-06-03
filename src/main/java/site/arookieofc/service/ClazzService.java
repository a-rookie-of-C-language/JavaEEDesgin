package site.arookieofc.service;

import site.arookieofc.entity.Clazz;

import java.util.List;
import java.util.Optional;

public interface ClazzService {

    List<Clazz> getAllClasses();

    Optional<Clazz> getClassById(String id);

    Integer addClass(Clazz clazz);

    Boolean updateClass(Clazz clazz);

    Boolean deleteClass(String id);

    List<Clazz> getClassesByTeacher(String teacherId);

    void updateStudentCount(String classId, int increment);

    int getStudentCount(String classId);

    Optional<String > getClassIdByName(String clazz);
}