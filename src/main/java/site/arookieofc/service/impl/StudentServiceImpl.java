package site.arookieofc.service.impl;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.transactional.Propagation;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.dao.StudentDAO;
import site.arookieofc.entity.Clazz;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
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
    public Optional<Student> getStudentById(String id) {
        if (id.isEmpty()) {
            return Optional.empty();
        }
        return studentDAO.getStudentById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void addStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("学生信息不能为空");
        }
        extracted(student);
        studentDAO.addStudent(UUID.randomUUID().toString().substring(0, 8), student.getName(), student.getAge(),
                student.getTeacherId(), student.getClazz());

        clazzService.updateStudentCount(student.getClazz(), 1);
    }

    private void extracted(Student student) {
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("学生姓名不能为空");
        }
        if (student.getAge() <= 0 || student.getAge() > 150) {
            throw new IllegalArgumentException("学生年龄必须在1-150之间");
        }
        if (student.getClazz() == null || student.getClazz().trim().isEmpty()) {
            throw new IllegalArgumentException("学生班级不能为空");
        }

        Optional<Clazz> clazz = clazzService.getClassById(student.getClazz());
        if (clazz.isEmpty()) {
            throw new IllegalArgumentException("指定的班级不存在");
        }

        if (student.getTeacherId() != null && !student.getTeacherId().trim().isEmpty()) {
            Optional<Teacher> teacher = teacherService.getTeacherById(student.getTeacherId());
            if (teacher.isEmpty()) {
                throw new IllegalArgumentException("指定的教师不存在");
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateStudent(Student student) {
        if (student == null || student.getId().isEmpty()) {
            throw new IllegalArgumentException("无效的学生信息");
        }
        extracted(student);

        Optional<Student> originalStudentOpt = getStudentById(student.getId());
        if (originalStudentOpt.isEmpty()) {
            throw new RuntimeException("学生不存在");
        }

        Student originalStudent = originalStudentOpt.get();
        String originalClass = originalStudent.getClazz();
        String newClass = student.getClazz();

        boolean updated = studentDAO.updateStudent(student.getName(), student.getAge(),
                student.getTeacherId(), student.getClazz(), student.getId());
        if (!updated) {
            throw new RuntimeException("更新学生信息失败");
        }

        if (!originalClass.equals(newClass)) {
            // 原班级 -1
            clazzService.updateStudentCount(originalClass, -1);
            // 新班级 +1
            clazzService.updateStudentCount(newClass, 1);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteStudent(String id) {
        if (id.isEmpty()) {
            throw new IllegalArgumentException("无效的学生ID");
        }

        Optional<Student> studentOpt = getStudentById(id);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("学生不存在");
        }

        Student student = studentOpt.get();
        String classId = student.getClazz();

        boolean deleted = studentDAO.deleteStudent(id);
        if (!deleted) {
            throw new RuntimeException("删除学生失败，可能学生不存在");
        }

        // 更新班级学生数量 -1
        if (classId != null && !classId.trim().isEmpty()) {
            clazzService.updateStudentCount(classId, -1);
        }
    }

    @Override
    public List<Student> getAllStudents() {
        List<Student> students = studentDAO.getAllStudents();
        return students != null ? students : Collections.emptyList();
    }

    @Override
    public PageResult<Student> getStudentsByPage(int page, int size) {
        if (page <= 0 || size <= 0) {
            return new PageResult<>(Collections.emptyList(), 0, page, size);
        }

        // 获取总数
        long total = getTotalStudentCount();

        // 获取所有学生然后进行分页（简单实现）
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
    @Transactional(propagation = Propagation.REQUIRED)
    public void addStudentsBatch(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return;
        }

        for (Student student : students) {
            addStudent(student);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteStudentsBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (String id : ids) {
            deleteStudent(id);
        }
    }
}
