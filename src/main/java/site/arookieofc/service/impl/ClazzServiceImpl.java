package site.arookieofc.service.impl;

import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.transactional.Transactional;
import site.arookieofc.annotation.transactional.Propagation;
import site.arookieofc.dao.ClazzDAO;
import site.arookieofc.entity.Clazz;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.processor.transaction.TransactionInterceptor;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Component
public class ClazzServiceImpl implements ClazzService {
    
    private final ClazzDAO clazzDAO = DAOFactory.getDAO(ClazzDAO.class);
    
    @Override
    public List<Clazz> getAllClasses() {
        try {
            List<Clazz> clazzes = clazzDAO.getAllClasses();
            return clazzes != null ? clazzes : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error getting all classes: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public Optional<Clazz> getClassById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return clazzDAO.getClassById(id);
        } catch (Exception e) {
            System.err.println("Error getting class by id: " + e.getMessage());
            // 从默认数据中查找
            return getAllClasses().stream()
                    .filter(clazz -> id.equals(clazz.getId()))
                    .findFirst();
        }
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
        
        // 验证教师ID是否存在
        TeacherService teacherService = TransactionInterceptor.createProxy(new TeacherServiceImpl());
        Optional<Teacher> teacher = teacherService.getTeacherById(clazz.getTeacherId());
        if (teacher.isEmpty()) {
            throw new IllegalArgumentException("指定的班主任不存在");
        }
        
        // 验证班级ID是否已存在
        Optional<Clazz> existingClazz = getClassById(clazz.getId());
        if (existingClazz.isPresent()) {
            throw new IllegalArgumentException("班级ID已存在");
        }
        
        try {
            int result = clazzDAO.addClass(clazz.getId(), clazz.getName(),
                                         clazz.getTeacherId(), clazz.getDescription());
            if (result <= 0) {
                throw new RuntimeException("添加班级失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("添加班级失败: " + e.getMessage(), e);
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
        
        // 验证班级是否存在
        Optional<Clazz> existingClazz = getClassById(clazz.getId());
        if (existingClazz.isEmpty()) {
            throw new IllegalArgumentException("班级不存在");
        }
        
        // 验证教师ID是否存在
        TeacherService teacherService = TransactionInterceptor.createProxy(new TeacherServiceImpl());
        Optional<Teacher> teacher = teacherService.getTeacherById(clazz.getTeacherId());
        if (teacher.isEmpty()) {
            throw new IllegalArgumentException("指定的班主任不存在");
        }
        
        try {
            boolean updated = clazzDAO.updateClass(clazz.getName(), clazz.getTeacherId(),
                                                  clazz.getDescription(), clazz.getId());
            if (!updated) {
                throw new RuntimeException("更新班级信息失败，可能班级不存在");
            }
        } catch (Exception e) {
            throw new RuntimeException("更新班级失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteClass(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("无效的班级ID");
        }
        
        // 检查班级是否存在
        Optional<Clazz> clazz = getClassById(id);
        if (clazz.isEmpty()) {
            throw new IllegalArgumentException("班级不存在");
        }
        
        // 检查班级是否有学生
        StudentService studentService = TransactionInterceptor.createProxy(new StudentServiceImpl());
        List<Student> students = studentService.getStudentsByClass(id);
        if (!students.isEmpty()) {
            throw new RuntimeException("班级中还有学生，无法删除");
        }
        
        try {
            boolean deleted = clazzDAO.deleteClass(id);
            if (!deleted) {
                throw new RuntimeException("删除班级失败，可能班级不存在");
            }
        } catch (Exception e) {
            throw new RuntimeException("删除班级失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Clazz> getClassesByTeacher(String teacherId) {
        if (teacherId == null || teacherId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            List<Clazz> clazzes = clazzDAO.getClassesByTeacher(teacherId);
            return clazzes != null ? clazzes : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error getting classes by teacher: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateStudentCount(String classId, int increment) {
        if (classId == null || classId.trim().isEmpty()) {
            throw new IllegalArgumentException("班级ID不能为空");
        }
        
        try {
            // 获取当前学生数量
            Optional<Clazz> clazzOpt = getClassById(classId);
            if (clazzOpt.isEmpty()) {
                throw new RuntimeException("班级不存在: " + classId);
            }
            
            Clazz clazz = clazzOpt.get();
            int currentCount = clazz.getStudentCount() != null ? clazz.getStudentCount() : 0;
            int newCount = Math.max(0, currentCount + increment); // 确保不为负数
            
            // 更新学生数量
            boolean updated = clazzDAO.updateStudentCount(classId, newCount);
            if (!updated) {
                throw new RuntimeException("更新班级学生数量失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("更新班级学生数量失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int getStudentCount(String classId) {
        Optional<Clazz> clazzOpt = getClassById(classId);
        return clazzOpt.map(clazz -> clazz.getStudentCount() != null ? clazz.getStudentCount() : 0)
                      .orElse(0);
    }
}