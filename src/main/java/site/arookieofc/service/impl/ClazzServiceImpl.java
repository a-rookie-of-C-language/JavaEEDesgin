package site.arookieofc.service.impl;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.annotation.validation.NotNullAndEmpty;
import site.arookieofc.annotation.validation.Range;
import site.arookieofc.dao.ClazzDAO;
import site.arookieofc.entity.Clazz;
import site.arookieofc.entity.Student;
import site.arookieofc.processor.validation.ValidationProcessor;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Component
@Slf4j
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
    @Transactional
    public Integer addClass(@NotNullAndEmpty Clazz clazz) {
        teacherService.getTeacherById(clazz.getTeacherId())
                .orElseThrow(() -> new RuntimeException("老师为空"));

        getClassById(clazz.getId())
                .orElseThrow(() -> new IllegalArgumentException("班级ID已存在"));

        return clazzDAO.addClass(clazz.getId(), clazz.getName(), clazz.getTeacherId());
    }

    @Override
    @Transactional
    public Boolean updateClass(@NotNullAndEmpty Clazz clazz) {
        getClassById(clazz.getId())
                .orElseThrow(() -> new RuntimeException("class ID不存在"));
        teacherService.getTeacherById(clazz.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("指定的班主任不存在"));

        return clazzDAO.updateClass(clazz.getName(), clazz.getTeacherId(), clazz.getId());
    }

    @Override
    @Transactional
    public Boolean deleteClass(@NotNullAndEmpty String id) {

        getClassById(id)
                .orElseThrow(() -> new IllegalArgumentException("班级不存在"));

        List<Student> students = studentService.getStudentsByClass(id);
        if (!students.isEmpty()) {
            throw new RuntimeException("班级中还有学生，无法删除");
        }


        return clazzDAO.deleteClass(id);
    }

    @Override
    public List<Clazz> getClassesByTeacher(@NotNullAndEmpty String teacherId) {
        List<Clazz> clazzes = clazzDAO.getClassesByTeacher(teacherId);
        return clazzes != null ? clazzes : Collections.emptyList();
    }

    @Override
    @Transactional
    public void updateStudentCount(@NotNullAndEmpty String classId,@Range(min = 0) int increment) {
        Clazz clazz = getClassById(classId).orElseThrow(() -> new IllegalArgumentException("班级不存在"));

        int currentCount = clazz.getStudentCount() != null ? clazz.getStudentCount() : 0;
        int newCount = Math.max(0, currentCount + increment);

        boolean updated = clazzDAO.updateStudentCount(newCount, classId);
        if (!updated) {
            throw new RuntimeException("更新班级学生数量失败");
        }
    }

    @Override
    public int getStudentCount(@NotNullAndEmpty String classId) {
        Optional<Clazz> clazzOpt = getClassById(classId);
        return clazzOpt.map(clazz -> clazz.getStudentCount() != null ? clazz.getStudentCount() : 0)
                .orElse(0);
    }

    @Override
    public Optional<String> getClassIdByName(@NotNullAndEmpty String clazz) {
        return clazzDAO.getClassIdByName(clazz);
    }
}