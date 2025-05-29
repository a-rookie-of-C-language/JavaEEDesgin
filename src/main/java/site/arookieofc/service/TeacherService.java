package site.arookieofc.service;

import site.arookieofc.entity.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherService {

    List<Teacher> getAllTeachers();

    Optional<Teacher> getTeacherById(String id);

    // 重命名方法
    List<String> getAllClassNames();

    void addTeacher(Teacher teacher);

    void updateTeacher(Teacher teacher);

    void deleteTeacher(String id);
}