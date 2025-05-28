package site.arookieofc.service.impl;

import site.arookieofc.dao.TeacherDAO;
import site.arookieofc.entity.Teacher;
import site.arookieofc.factory.DAOFactory;
import site.arookieofc.service.TeacherService;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class TeacherServiceImpl implements TeacherService {
    
    private final TeacherDAO teacherDAO = DAOFactory.getDAO(TeacherDAO.class);
    
    @Override
    public List<Teacher> getAllTeachers() {
        try {
            return teacherDAO.getAllTeachers();
        } catch (Exception e) {
            // 如果数据库中没有教师表，返回默认数据
            List<Teacher> defaultTeachers = new ArrayList<>();
            defaultTeachers.add(new Teacher("T001", "张老师"));
            defaultTeachers.add(new Teacher("T002", "李老师"));
            defaultTeachers.add(new Teacher("T003", "王老师"));
            defaultTeachers.add(new Teacher("T004", "刘老师"));
            defaultTeachers.add(new Teacher("T005", "陈老师"));
            return defaultTeachers;
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
            // 如果数据库查询失败，从默认数据中查找
            return getAllTeachers().stream()
                    .filter(teacher -> id.equals(teacher.getId()))
                    .findFirst();
        }
    }
    
    @Override
    public List<String> getAllClasses() {
        try {
            return teacherDAO.getAllClasses();
        } catch (Exception e) {
            // 如果数据库查询失败，返回默认班级列表
            List<String> defaultClasses = new ArrayList<>();
            defaultClasses.add("计算机1班");
            defaultClasses.add("计算机2班");
            defaultClasses.add("软件1班");
            defaultClasses.add("软件2班");
            defaultClasses.add("网络1班");
            return defaultClasses;
        }
    }
    
    @Override
    public void addTeacher(Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("教师信息不能为空");
        }
        if (teacher.getName() == null || teacher.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("教师姓名不能为空");
        }
        // 实现添加教师的逻辑
        // 注意：需要在 TeacherDAO 中添加相应的方法
    }
    
    @Override
    public void updateTeacher(Teacher teacher) {
        if (teacher == null || teacher.getId() == null) {
            throw new IllegalArgumentException("无效的教师信息");
        }
        // 实现更新教师的逻辑
    }
    
    @Override
    public void deleteTeacher(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("无效的教师ID");
        }
        // 实现删除教师的逻辑
    }
}