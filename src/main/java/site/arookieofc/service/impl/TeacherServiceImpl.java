package site.arookieofc.service.impl;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.annotation.validation.NotNullAndEmpty;
import site.arookieofc.dao.TeacherDAO;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;

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
        return teacherDAO.getAllTeachers();
    }

    @Override
    public Optional<Teacher> getTeacherById(@NotNullAndEmpty String id) {
        return teacherDAO.getTeacherById(id);
    }

    @Override
    public List<String> getAllClassNames() {
        return teacherDAO.getAllClassNames();
    }

    @Override
    @Transactional
    public void addTeacher(@NotNullAndEmpty Teacher teacher) {
        teacherDAO.addTeacher(
                teacher.getId(),
                teacher.getName()
        );
    }

    @Override
    @Transactional
    public void updateTeacher(@NotNullAndEmpty Teacher teacher) {
        teacherDAO.updateTeacher(
                teacher.getName(),
                teacher.getId()
        );
    }

    @Override
    @Transactional
    public void deleteTeacher(@NotNullAndEmpty String id) {
        List<Student> students = studentService.getStudentsByTeacher(id);
        if (!students.isEmpty()) {
            throw new RuntimeException("该教师下还有学生，无法删除");
        }
        teacherDAO.deleteTeacher(id);

    }

    @Override
    @Transactional
    public void addTeacherThrowable(@NotNullAndEmpty Teacher teacher) {
        teacherDAO.addTeacher(
                teacher.getId(),
                teacher.getName()
        );
        throw new RuntimeException("测试回滚");
    }
}