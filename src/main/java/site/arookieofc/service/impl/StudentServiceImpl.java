package site.arookieofc.service.impl;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.annotation.validation.Need;
import site.arookieofc.annotation.validation.Range;
import site.arookieofc.dao.StudentDAO;
import site.arookieofc.pojo.DO.Student;
import site.arookieofc.pojo.dto.PageResult;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private ClazzService clazzService;

    @Autowired
    private TeacherService teacherService;

    @Override
    public Student getStudentById(@Need String id) {
        return studentDAO.getStudentById(id)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
    }

    @Override
    @Transactional
    public void addStudent(@Need Student student) {
        String classId = student.getClazzId();
        teacherService
                .getTeacherById(student.getTeacherId());
        studentDAO.addStudent(student.getId(),
                student.getName(),
                student.getAge(),
                student.getTeacherId(), classId);
        clazzService.updateStudentCount(classId, 1);
    }

    @Override
    @Transactional
    public void updateStudent(@Need Student student) {
        Student originalStudent = getStudentById(student.getId());
        String newClassId = student.getClazzId();
        teacherService.getTeacherById(student.getTeacherId());
        String originalClassId = originalStudent.getClazzId();
        studentDAO.updateStudent(
                student.getName(),
                student.getAge(),
                student.getTeacherId(),
                newClassId,
                student.getId()
        );
        clazzService.updateStudentCount(originalClassId, -1);
        clazzService.updateStudentCount(newClassId, 1);
    }

    @Override
    @Transactional
    public void deleteStudent(@Need String id) {
        Student student = getStudentById(id);
        String classId = student.getClazzId();
        studentDAO.deleteStudent(id);
        clazzService.updateStudentCount(classId, -1);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentDAO.getAllStudents()
                .orElseThrow(() -> new IllegalArgumentException("没有学生"));
    }

    @Override
    public PageResult<Student> getStudentsByPage(@Range(min = 1) int page,
                                                 @Range(min = 1) int size) {
        long total = getTotalStudentCount();
        List<Student> allStudents = getAllStudents();
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, allStudents.size());
        List<Student> pageData;
        pageData = allStudents.subList(startIndex, endIndex);
        return new PageResult<>(pageData, total, page, size);
    }

    @Override
    public long getTotalStudentCount() {
        return studentDAO.getTotalStudentCount();
    }

    @Override
    public List<Student> getStudentsByClass(String clazz) {
        return studentDAO.getStudentsByClass(clazz)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Student> getStudentsByTeacher(String teacherId) {
        return studentDAO.getStudentsByTeacher(teacherId)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Student> getStudentsByTeacherId(String teacherId) {
        return studentDAO.getStudentsByTeacherId(teacherId)
                .orElse(Collections.emptyList());  // 返回空列表而不是null
    }

    @Override
    @Transactional
    public void updateStudentsTeacherByClass(String teacherId, String clazzId) {
        studentDAO.updateStudentsTeacherByClass(teacherId, clazzId);
    }
}
