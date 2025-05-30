package site.arookieofc.service;

import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.entity.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherService {

    List<Teacher> getAllTeachers();

    Optional<Teacher> getTeacherById(String id);

    List<String> getAllClassNames();

    void addTeacher(Teacher teacher);

    void updateTeacher(Teacher teacher);

    void deleteTeacher(String id);
}