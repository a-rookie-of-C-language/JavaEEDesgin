package site.arookieofc.service.impl;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.annotation.validation.Need;
import site.arookieofc.dao.ClazzDAO;
import site.arookieofc.pojo.DO.Clazz;
import site.arookieofc.pojo.DO.Student;
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
        return clazzDAO.getAllClasses()
                .orElse(Collections.emptyList());
    }

    @Override
    public Clazz getClassById(@Need String id) {
        return clazzDAO.getClassById(id)
                .orElseThrow(() -> new IllegalArgumentException("班级不存在"));
    }

    @Override
    public List<Clazz> getClassesByTeacher(@Need String teacherId) {
        return clazzDAO.getClassesByTeacher(teacherId)
                .orElse(Collections.emptyList());
    }

    @Override
    public String getClassIdByName(@Need String clazz) {
        return clazzDAO.getClassIdByName(clazz)
                .orElseThrow(() -> new IllegalArgumentException("班级名称不存在"));
    }

    @Override
    @Transactional
    public Integer addClass(@Need Clazz clazz) {
        teacherService.getTeacherById(clazz.getTeacherId());
        return clazzDAO.addClass(clazz.getId(), clazz.getName(), clazz.getTeacherId());
    }

    @Override
    @Transactional
    public Boolean updateClass(@Need Clazz clazz) {
        getClassById(clazz.getId());
        teacherService.getTeacherById(clazz.getTeacherId());
        Boolean result = clazzDAO
                .updateClass(clazz.getName(), clazz.getTeacherId(), clazz.getId());
        if (result) {
            studentService
                    .updateStudentsTeacherByClass(clazz.getTeacherId(), clazz.getId());
        }
        return result;
    }

    @Override
    @Transactional
    public Boolean deleteClass(@Need String id) {
        getClassById(id);
        Optional<List<Student>> studentsOpt = Optional
                .ofNullable(studentService.getStudentsByClass(id));
        studentsOpt.ifPresent(students1 -> {
            throw new IllegalArgumentException("班级中还有学生");
        });
        return clazzDAO.deleteClass(id);
    }


    @Override
    @Transactional
    public void updateStudentCount(@Need String classId, int increment) {
        Clazz clazz = getClassById(classId);
        int currentCount = clazz.getStudentCount() != null ? clazz.getStudentCount() : 0;
        int newCount = Math.max(0, currentCount + increment);
        clazzDAO.updateStudentCount(newCount, classId);
    }

    @Override
    public int getStudentCount(@Need String classId) {
        Clazz clazz = getClassById(classId);
        return clazz.getStudentCount();
    }
}