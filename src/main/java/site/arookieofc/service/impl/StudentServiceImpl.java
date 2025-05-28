package site.arookieofc.service.impl;

import site.arookieofc.dao.StudentDAO;
import site.arookieofc.entity.Student;
import site.arookieofc.factory.DAOFactory;
import site.arookieofc.service.StudentService;
import site.arookieofc.pojo.dto.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

public class StudentServiceImpl implements StudentService {
    
    private final StudentDAO studentDAO = DAOFactory.getDAO(StudentDAO.class);
    
    @Override
    public Optional<Student> getStudentById(int id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return studentDAO.getStudentById(id);
    }
    
    @Override
    public void addStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("学生信息不能为空");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("学生姓名不能为空");
        }
        if (student.getAge() <= 0 || student.getAge() > 150) {
            throw new IllegalArgumentException("学生年龄必须在1-150之间");
        }
        
        studentDAO.addStudent(student.getName(), student.getAge(), 
                            student.getTeacherId(), student.getClazz());
    }
    
    @Override
    public void updateStudent(Student student) {
        if (student == null || student.getId() <= 0) {
            throw new IllegalArgumentException("无效的学生信息");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("学生姓名不能为空");
        }
        if (student.getAge() <= 0 || student.getAge() > 150) {
            throw new IllegalArgumentException("学生年龄必须在1-150之间");
        }
        
        boolean updated = studentDAO.updateStudent(student.getName(), student.getAge(), 
                                                 student.getTeacherId(), student.getClazz(), student.getId());
        if (!updated) {
            throw new RuntimeException("更新学生信息失败");
        }
    }
    
    @Override
    public void deleteStudent(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("无效的学生ID");
        }
        
        boolean deleted = studentDAO.deleteStudent(id);
        if (!deleted) {
            throw new RuntimeException("删除学生失败，可能学生不存在");
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
            List<Student> allStudents = getAllStudents();
            return allStudents != null ? allStudents.size() : 0;
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
}
