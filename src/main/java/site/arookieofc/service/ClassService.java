package site.arookieofc.service;

import site.arookieofc.entity.Class;

import java.util.List;
import java.util.Optional;

public interface ClassService {

    List<Class> getAllClasses();

    Optional<Class> getClassById(String id);

    void addClass(Class clazz);

    void updateClass(Class clazz);

    void deleteClass(String id);

    List<Class> getClassesByTeacher(String teacherId);
}