package site.arookieofc.service.impl;

import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.annotation.transactional.Propagation;
import site.arookieofc.dao.StudentDAO;
import site.arookieofc.entity.Clazz;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.ClazzService;
import site.arookieofc.pojo.dto.PageResult;
import site.arookieofc.service.TeacherService;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.UUID;

@Component
public class StudentServiceImpl implements StudentService {

    private final StudentDAO studentDAO = DAOFactory.getDAO(StudentDAO.class);

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

        studentDAO.addStudent(UUID.randomUUID().toString(), student.getName(), student.getAge(),
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

        // 验证班级是否存在
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

        // 更新学生信息
        boolean updated = studentDAO.updateStudent(student.getName(), student.getAge(),
                student.getTeacherId(), student.getClazz(), student.getId());
        if (!updated) {
            throw new RuntimeException("更新学生信息失败");
        }

        // 如果班级发生变化，需要同步更新两个班级的学生数量
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

        // 获取学生信息（用于获取班级）
        Optional<Student> studentOpt = getStudentById(id);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("学生不存在");
        }

        Student student = studentOpt.get();
        String classId = student.getClazz();

        // 删除学生
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
        try {
            List<Student> students = studentDAO.getAllStudents();
            return students != null ? students : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error getting all students: " + e.getMessage());
            return Collections.emptyList();
        }
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
        try {
            return studentDAO.getTotalStudentCount();
        } catch (Exception e) {
            System.err.println("Error getting student count: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Student> getStudentsByClass(String clazz) {
        try {
            return studentDAO.getStudentsByClass(clazz);
        } catch (Exception e) {
            System.err.println("Error getting students by class: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Student> getStudentsByTeacher(String teacherId) {
        try {
            return studentDAO.getStudentsByTeacher(teacherId);
        } catch (Exception e) {
            System.err.println("Error getting students by teacher: " + e.getMessage());
            return Collections.emptyList();
        }
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
            addStudent(student); // 复用已有的添加方法，保持事务一致性
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteStudentsBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (String id : ids) {
            deleteStudent(id); // 复用已有的删除方法，保持事务一致性
        }
    }
}
