package site.arookieofc.service;

import site.arookieofc.entity.Class;

import java.util.List;
import java.util.Optional;

public interface ClassService {
    
    /**
     * 获取所有班级
     */
    List<Class> getAllClasses();
    
    /**
     * 根据ID获取班级
     */
    Optional<Class> getClassById(String id);
    
    /**
     * 添加班级
     */
    void addClass(Class clazz);
    
    /**
     * 更新班级信息
     */
    void updateClass(Class clazz);
    
    /**
     * 删除班级
     */
    void deleteClass(String id);
    
    /**
     * 根据教师ID获取班级列表
     */
    List<Class> getClassesByTeacher(String teacherId);
}