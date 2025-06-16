package site.arookieofc.service.impl;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.validation.Need;
import site.arookieofc.dao.TeacherDAO;
import site.arookieofc.pojo.DO.Student;
import site.arookieofc.pojo.DO.Teacher;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherDAO teacherDAO;

    @Autowired
    private StudentService studentService;

    @Override
    public List<Teacher> getAllTeachers() {
        return teacherDAO.getAllTeachers()
                .orElse(Collections.emptyList());
    }

    @Override
    public List<String> getAllClassNames() {
        return teacherDAO.getAllClassNames()
                .orElse(Collections.emptyList());
    }

    @Override
    public Teacher getTeacherById(@Need String id) {
        return teacherDAO.getTeacherById(id)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
    }

    @Override
    public void addTeacher(@Need Teacher teacher) {
        teacherDAO.addTeacher(
                teacher.getId(),
                teacher.getName()
        );
    }

    @Override
    public void updateTeacher(@Need Teacher teacher) {
        teacherDAO.updateTeacher(
                teacher.getName(),
                teacher.getId()
        );
    }

    @Override
    public void deleteTeacher(@Need String id) {
        List<Student> students = studentService.getStudentsByTeacher(id);
        if (students != null) {
            throw new RuntimeException("该教师下还有学生，无法删除");
        }
        teacherDAO.deleteTeacher(id);


    }

    @Override
    public void addTeacherThrowable(@Need Teacher teacher) {
        teacherDAO.addTeacher(
                teacher.getId(),
                teacher.getName()
        );
        throw new RuntimeException("测试回滚");
    }
}