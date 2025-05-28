package site.arookieofc.service;

import site.arookieofc.entity.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherService {
    
    /**
     * 获取所有教师
     */
    List<Teacher> getAllTeachers();
    
    /**
     * 根据ID获取教师
     */
    Optional<Teacher> getTeacherById(String id);
    
    /**
     * 获取所有班级
     */
    List<String> getAllClasses();
    
    /**
     * 添加教师
     */
    void addTeacher(Teacher teacher);
    
    /**
     * 更新教师信息
     */
    void updateTeacher(Teacher teacher);
    
    /**
     * 删除教师
     */
    void deleteTeacher(String id);
}