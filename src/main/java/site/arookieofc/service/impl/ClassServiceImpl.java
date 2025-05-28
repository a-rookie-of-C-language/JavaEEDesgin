package site.arookieofc.service.impl;

import site.arookieofc.dao.ClassDAO;
import site.arookieofc.entity.Class;
import site.arookieofc.factory.DAOFactory;
import site.arookieofc.service.ClassService;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.ArrayList;

public class ClassServiceImpl implements ClassService {
    
    private final ClassDAO classDAO = DAOFactory.getDAO(ClassDAO.class);
    
    @Override
    public List<Class> getAllClasses() {
        try {
            List<Class> classes = classDAO.getAllClasses();
            return classes != null ? classes : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error getting all classes: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public Optional<Class> getClassById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return classDAO.getClassById(id);
        } catch (Exception e) {
            System.err.println("Error getting class by id: " + e.getMessage());
            // 从默认数据中查找
            return getAllClasses().stream()
                    .filter(clazz -> id.equals(clazz.getId()))
                    .findFirst();
        }
    }
    
    @Override
    public void addClass(Class clazz) {
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
        
        try {
            int result = classDAO.addClass(clazz.getId(), clazz.getName(), 
                                         clazz.getTeacherId(), clazz.getDescription());
            if (result <= 0) {
                throw new RuntimeException("添加班级失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("添加班级失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void updateClass(Class clazz) {
        if (clazz == null || clazz.getId() == null || clazz.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("无效的班级信息");
        }
        if (clazz.getName() == null || clazz.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("班级名称不能为空");
        }
        if (clazz.getTeacherId() == null || clazz.getTeacherId().trim().isEmpty()) {
            throw new IllegalArgumentException("班主任ID不能为空");
        }
        
        try {
            boolean updated = classDAO.updateClass(clazz.getName(), clazz.getTeacherId(), 
                                                  clazz.getDescription(), clazz.getId());
            if (!updated) {
                throw new RuntimeException("更新班级信息失败，可能班级不存在");
            }
        } catch (Exception e) {
            throw new RuntimeException("更新班级失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteClass(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("无效的班级ID");
        }
        
        try {
            boolean deleted = classDAO.deleteClass(id);
            if (!deleted) {
                throw new RuntimeException("删除班级失败，可能班级不存在");
            }
        } catch (Exception e) {
            throw new RuntimeException("删除班级失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Class> getClassesByTeacher(String teacherId) {
        if (teacherId == null || teacherId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            List<Class> classes = classDAO.getClassesByTeacher(teacherId);
            return classes != null ? classes : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error getting classes by teacher: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}