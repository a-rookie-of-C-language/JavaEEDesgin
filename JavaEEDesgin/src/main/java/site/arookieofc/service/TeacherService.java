package site.arookieofc.service;

import site.arookieofc.pojo.DO.Teacher;
import java.util.List;
import java.util.Optional;

public interface TeacherService {

    List<Teacher> getAllTeachers();

    Teacher getTeacherById(String id);

    List<String> getAllClassNames();

    void addTeacher(Teacher teacher);

    void updateTeacher(Teacher teacher);

    void deleteTeacher(String id);

    void addTeacherThrowable(Teacher teacher);
}