package site.arookieofc.service.impl;

import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.annotation.transactional.Propagation;
import site.arookieofc.dao.ClazzDAO;
import site.arookieofc.entity.Clazz;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Component
public class ClazzServiceImpl implements ClazzService {

    @Autowired
    private ClazzDAO clazzDAO;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Override
    public List<Clazz> getAllClasses() {
        List<Clazz> clazzes = clazzDAO.getAllClasses();
        return clazzes != null ? clazzes : Collections.emptyList();
    }

    @Override
    public Optional<Clazz> getClassById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return clazzDAO.getClassById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void addClass(Clazz clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("班级信息不能为空");
        }
        if (clazz.getId() == null || clazz.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("班级ID不能为空");
        }
        if (clazz.getName() == null || clazz.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("班级名称不能为空");
        }
        if (clazz.getTeacherId() == null || clazz.getTeacherId().trim().isEmpty()) {
            throw new IllegalArgumentException("班主任ID不能为空");
        }

        Optional<Teacher> teacher = teacherService.getTeacherById(clazz.getTeacherId());
        if (teacher.isEmpty()) {
            throw new IllegalArgumentException("指定的班主任不存在");
        }

        Optional<Clazz> existingClazz = getClassById(clazz.getId());
        if (existingClazz.isPresent()) {
            throw new IllegalArgumentException("班级ID已存在");
        }


        int result = clazzDAO.addClass(clazz.getId(), clazz.getName(),
                clazz.getTeacherId(), clazz.getDescription());
        if (result <= 0) {
            throw new RuntimeException("添加班级失败");
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateClass(Clazz clazz) {
        if (clazz == null || clazz.getId() == null || clazz.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("无效的班级信息");
        }
        if (clazz.getName() == null || clazz.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("班级名称不能为空");
        }
        if (clazz.getTeacherId() == null || clazz.getTeacherId().trim().isEmpty()) {
            throw new IllegalArgumentException("班主任ID不能为空");
        }

        Optional<Clazz> existingClazz = getClassById(clazz.getId());
        if (existingClazz.isEmpty()) {
            throw new IllegalArgumentException("班级不存在");
        }

        Optional<Teacher> teacher = teacherService.getTeacherById(clazz.getTeacherId());
        if (teacher.isEmpty()) {
            throw new IllegalArgumentException("指定的班主任不存在");
        }


        boolean updated = clazzDAO.updateClass(clazz.getName(), clazz.getTeacherId(),
                clazz.getDescription(), clazz.getId());
        if (!updated) {
            throw new RuntimeException("更新班级信息失败，可能班级不存在");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteClass(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("无效的班级ID");
        }

        Optional<Clazz> clazz = getClassById(id);
        if (clazz.isEmpty()) {
            throw new IllegalArgumentException("班级不存在");
        }

        List<Student> students = studentService.getStudentsByClass(id);
        if (!students.isEmpty()) {
            throw new RuntimeException("班级中还有学生，无法删除");
        }


        boolean deleted = clazzDAO.deleteClass(id);
        if (!deleted) {
            throw new RuntimeException("删除班级失败，可能班级不存在");
        }
    }

    @Override
    public List<Clazz> getClassesByTeacher(String teacherId) {
        if (teacherId == null || teacherId.trim().isEmpty()) {
            return Collections.emptyList();
        }


        List<Clazz> clazzes = clazzDAO.getClassesByTeacher(teacherId);
        return clazzes != null ? clazzes : Collections.emptyList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateStudentCount(String classId, int increment) {
        if (classId == null || classId.trim().isEmpty()) {
            throw new IllegalArgumentException("班级ID不能为空");
        }


        Optional<Clazz> clazzOpt = getClassById(classId);
        if (clazzOpt.isEmpty()) {
            throw new RuntimeException("班级不存在: " + classId);
        }

        Clazz clazz = clazzOpt.get();
        int currentCount = clazz.getStudentCount() != null ? clazz.getStudentCount() : 0;
        int newCount = Math.max(0, currentCount + increment); // 确保不为负数

        boolean updated = clazzDAO.updateStudentCount(newCount, classId);
        if (!updated) {
            throw new RuntimeException("更新班级学生数量失败");
        }
    }

    @Override
    public int getStudentCount(String classId) {
        Optional<Clazz> clazzOpt = getClassById(classId);
        return clazzOpt.map(clazz -> clazz.getStudentCount() != null ? clazz.getStudentCount() : 0)
                .orElse(0);
    }
}