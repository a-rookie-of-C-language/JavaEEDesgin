package site.arookieofc.service;

import site.arookieofc.entity.Clazz;

import java.util.List;
import java.util.Optional;

public interface ClazzService {

    List<Clazz> getAllClasses();

    Optional<Clazz> getClassById(String id);

    void addClass(Clazz clazz);

    void updateClass(Clazz clazz);

    void deleteClass(String id);

    List<Clazz> getClassesByTeacher(String teacherId);
    
    /**
     * 更新班级学生数量
     * @param classId 班级ID
     * @param increment 增量（正数表示增加，负数表示减少）
     */
    void updateStudentCount(String classId, int increment);
    
    /**
     * 获取班级当前学生数量
     * @param classId 班级ID
     * @return 学生数量
     */
    int getStudentCount(String classId);
}