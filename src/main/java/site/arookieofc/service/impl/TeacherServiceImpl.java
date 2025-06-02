package site.arookieofc.service.impl;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.transactional.Propagation;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.dao.TeacherDAO;
import site.arookieofc.entity.Clazz;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TeacherServiceImpl implements TeacherService {

    private final TeacherDAO teacherDAO = DAOFactory.getDAO(TeacherDAO.class);

    @Autowired
    private StudentService studentService;

    @Autowired
    private ClazzService clazzService;

    @Override
    public List<Teacher> getAllTeachers() {
        try {
            return teacherDAO.getAllTeachers();
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<Teacher> getTeacherById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return teacherDAO.getTeacherById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> getAllClassNames() {
        try {
            return teacherDAO.getAllClassNames();
        } catch (Exception e) {
            e.fillInStackTrace();
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void addTeacher(Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("教师信息不能为空");
        }
        if (teacher.getName() == null || teacher.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("教师姓名不能为空");
        }

        try {
            int result = teacherDAO.addTeacher(
                    teacher.getId(),
                    teacher.getName(),
                    teacher.getDepartment()
            );

            if (result <= 0) {
                throw new RuntimeException("添加教师失败");
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            throw new RuntimeException("数据库错误", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateTeacher(Teacher teacher) {
        if (teacher == null || teacher.getId() == null) {
            throw new IllegalArgumentException("无效的教师信息");
        }

        // 验证教师是否存在
        Optional<Teacher> existingTeacher = getTeacherById(teacher.getId());
        if (existingTeacher.isEmpty()) {
            throw new RuntimeException("教师不存在，无法更新");
        }

        try {
            boolean updated = teacherDAO.updateTeacher(
                    teacher.getName(),
                    teacher.getDepartment(),
                    teacher.getId()
            );

            if (!updated) {
                throw new RuntimeException("更新教师失败，可能教师不存在");
            }
        } catch (Exception e) {
            throw new RuntimeException("数据库错误", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteTeacher(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("无效的教师ID");
        }

        // 检查是否有学生关联到这个教师
        List<Student> students = studentService.getStudentsByTeacher(id);
        if (!students.isEmpty()) {
            throw new RuntimeException("该教师下还有学生，无法删除");
        }

        // 检查是否有班级关联到这个教师
        List<Clazz> classes = clazzService.getClassesByTeacher(id);
        if (!classes.isEmpty()) {
            throw new RuntimeException("该教师还是班主任，无法删除");
        }

        try {
            boolean deleted = teacherDAO.deleteTeacher(id);

            if (!deleted) {
                throw new RuntimeException("删除教师失败，可能教师不存在");
            }
        } catch (Exception e) {
            log.error("数据库错误: {}", e.getMessage());
            throw new RuntimeException("数据库错误", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void reassignTeacherClasses(String oldTeacherId, String newTeacherId) {
        if (oldTeacherId == null || oldTeacherId.trim().isEmpty()) {
            throw new IllegalArgumentException("原教师ID不能为空");
        }
        if (newTeacherId == null || newTeacherId.trim().isEmpty()) {
            throw new IllegalArgumentException("新教师ID不能为空");
        }

        // 验证两个教师是否存在
        Optional<Teacher> oldTeacher = getTeacherById(oldTeacherId);
        if (oldTeacher.isEmpty()) {
            throw new IllegalArgumentException("原教师不存在");
        }

        Optional<Teacher> newTeacher = getTeacherById(newTeacherId);
        if (newTeacher.isEmpty()) {
            throw new IllegalArgumentException("新教师不存在");
        }

        // 获取原教师的所有班级
        List<Clazz> classes = clazzService.getClassesByTeacher(oldTeacherId);

        // 更新每个班级的教师ID
        for (Clazz clazz : classes) {
            clazz.setTeacherId(newTeacherId);
            clazzService.updateClass(clazz);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void addTeacherThrowable(Teacher teacher){
        teacherDAO.addTeacher(
                teacher.getId(),
                teacher.getName(),
                teacher.getDepartment()
        );
        throw new RuntimeException("测试回滚");
    }
}