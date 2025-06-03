package site.arookieofc.service.impl;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.annotation.validation.NotNullAndEmpty;
import site.arookieofc.annotation.validation.Range;
import site.arookieofc.dao.StudentDAO;
import site.arookieofc.entity.Student;
import site.arookieofc.pojo.dto.PageResult;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<Student> getStudentById(@NotNullAndEmpty String id) {
        return studentDAO.getStudentById(id);
    }

    @Override
    @Transactional
    public void addStudent(@NotNullAndEmpty Student student) {
        // 验证班级存在性并获取班级ID
        String classId = clazzService
                .getClassIdByName(student.getClazz())
                .orElseThrow(() -> new IllegalArgumentException("班级不存在"));
        teacherService
                .getTeacherById(student.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        studentDAO.addStudent(UUID.randomUUID().toString().substring(0, 8)
                , student.getName()
                , student.getAge()
                , student.getTeacherId(), classId);
        clazzService.updateStudentCount(classId, 1);
    }

    @Override
    @Transactional
    public void updateStudent(@NotNullAndEmpty Student student) {
        // 验证学生存在性
        Student originalStudent = getStudentById(student.getId())
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        // 验证班级存在性并获取班级ID
        String newClassId = clazzService.getClassIdByName(student.getClazz())
                .orElseThrow(() -> new IllegalArgumentException("无法找到班级"));

        teacherService.getTeacherById(student.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("找不到教师"));

        Optional<String> originalClassIdOpt = clazzService
                .getClassIdByName(originalStudent.getClazz());

        studentDAO.updateStudent(
                student.getName(),
                student.getAge(),
                student.getTeacherId(),
                newClassId,
                student.getId()
        );

        if (originalClassIdOpt.isPresent() && !originalClassIdOpt.get().equals(newClassId)) {
            clazzService.updateStudentCount(originalClassIdOpt.get(), -1);
            clazzService.updateStudentCount(newClassId, 1);
        }
    }

    @Override
    @Transactional
    public void deleteStudent(@NotNullAndEmpty String id) {

        Student student = getStudentById(id)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));

        Optional<String> classIdOpt = clazzService.getClassIdByName(student.getClazz());

        studentDAO.deleteStudent(id);
        // 更新班级学生数量 -1
        classIdOpt.ifPresent(s -> clazzService.updateStudentCount(s, -1));
    }

    @Override
    public List<Student> getAllStudents() {
        List<Student> students = studentDAO.getAllStudents();
        return students != null ? students : Collections.emptyList();
    }

    @Override
    public PageResult<Student> getStudentsByPage(@Range(min = 1) int page,
                                                 @Range(min = 1) int size) {

        long total = getTotalStudentCount();
        List<Student> allStudents = getAllStudents();
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, allStudents.size());
        List<Student> pageData;
        if (startIndex >= allStudents.size()) {
            pageData = Collections.emptyList();
        } else {
            pageData = allStudents.subList(startIndex, endIndex);
        }
        return new PageResult<>(pageData, total, page, size);
    }

    @Override
    public long getTotalStudentCount() {
        return studentDAO.getTotalStudentCount();
    }

    @Override
    public List<Student> getStudentsByClass(String clazz) {
        return studentDAO.getStudentsByClass(clazz);
    }

    @Override
    public List<Student> getStudentsByTeacher(String teacherId) {
        return studentDAO.getStudentsByTeacher(teacherId);
    }

    @Override
    public List<Student> getStudentsByTeacherId(String teacherId) {
        return List.of();
    }

    @Override
    @Transactional
    public void addStudentsBatch(@NotNullAndEmpty List<Student> students) {
        for (Student student : students) {
            addStudent(student);
        }
    }

    @Override
    @Transactional
    public void deleteStudentsBatch(@NotNullAndEmpty List<String> ids) {
        for (String id : ids) {
            deleteStudent(id);
        }
    }
}
