package site.arookieofc.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import site.arookieofc.dao.StudentDAO;
import site.arookieofc.dao.TeacherDAO;
import site.arookieofc.dao.ClazzDAO;
import site.arookieofc.entity.Student;
import site.arookieofc.entity.Teacher;
import site.arookieofc.entity.Clazz;
import site.arookieofc.processor.sql.DAOFactory;
import site.arookieofc.service.FunctionCallService;

import java.util.*;
import java.util.function.Function;

public class FunctionCallServiceImpl implements FunctionCallService {
    
    private final Map<String, Function<Map<String, Object>, Object>> functions = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final StudentDAO studentDAO = DAOFactory.getDAO(StudentDAO.class);
    private final TeacherDAO teacherDAO = DAOFactory.getDAO(TeacherDAO.class);
    private final ClazzDAO clazzDAO = DAOFactory.getDAO(ClazzDAO.class);
    
    public FunctionCallServiceImpl() {
        registerDefaultFunctions();
    }
    
    private void registerDefaultFunctions() {
        // 学生相关操作
        functions.put("get_student_by_id", params -> {
            int id = (Integer) params.get("id");
            return studentDAO.getStudentById(id).orElse(null);
        });
        
        functions.put("get_all_students", params -> studentDAO.getAllStudents());
        
        functions.put("add_student", params -> {
            String name = (String) params.get("name");
            int age = (Integer) params.get("age");
            String teacherId = (String) params.get("teacherId");
            String clazz = (String) params.get("clazz");
            return studentDAO.addStudent(name, age, teacherId, clazz);
        });
        
        functions.put("update_student", params -> {
            int id = (Integer) params.get("id");
            String name = (String) params.get("name");
            int age = (Integer) params.get("age");
            String teacherId = (String) params.get("teacherId");
            String clazz = (String) params.get("clazz");
            return studentDAO.updateStudent(name, age, teacherId, clazz, id);
        });
        
        functions.put("delete_student", params -> {
            int id = (Integer) params.get("id");
            return studentDAO.deleteStudent(id);
        });
        
        // 教师相关操作
        functions.put("get_teacher_by_id", params -> {
            String id = (String) params.get("id");
            return teacherDAO.getTeacherById(id).orElse(null);
        });
        
        functions.put("get_all_teachers", params -> teacherDAO.getAllTeachers());
        
        functions.put("add_teacher", params -> {
            String id = (String) params.get("id");
            String name = (String) params.get("name");
            String department = (String) params.get("department");
            return teacherDAO.addTeacher(id, name, department);
        });
        
        // 班级相关操作
        functions.put("get_class_by_id", params -> {
            String id = (String) params.get("id");
            return clazzDAO.getClassById(id).orElse(null);
        });
        
        functions.put("get_all_classes", params -> clazzDAO.getAllClasses());
        
        functions.put("add_class", params -> {
            String id = (String) params.get("id");
            String name = (String) params.get("name");
            String teacherId = (String) params.get("teacherId");
            String description = (String) params.get("description");
            return clazzDAO.addClass(id, name, teacherId, description);
        });
    }
    
    @Override
    public Object executeFunction(String functionName, Map<String, Object> parameters) {
        Function<Map<String, Object>, Object> function = functions.get(functionName);
        if (function == null) {
            throw new IllegalArgumentException("未知的函数: " + functionName);
        }
        
        try {
            return function.apply(parameters);
        } catch (Exception e) {
            throw new RuntimeException("执行函数失败: " + functionName, e);
        }
    }
    
    @Override
    public Map<String, Object> getAvailableFunctions() {
        Map<String, Object> availableFunctions = new HashMap<>();
        
        // 学生管理函数
        availableFunctions.put("get_student_by_id", Map.of(
            "description", "根据ID获取学生信息",
            "parameters", Map.of(
                "type", "object",
                "properties", Map.of(
                    "id", Map.of("type", "integer", "description", "学生ID")
                ),
                "required", List.of("id")
            )
        ));
        
        availableFunctions.put("get_all_students", Map.of(
            "description", "获取所有学生信息",
            "parameters", Map.of("type", "object", "properties", Map.of())
        ));
        
        availableFunctions.put("add_student", Map.of(
            "description", "添加新学生",
            "parameters", Map.of(
                "type", "object",
                "properties", Map.of(
                    "name", Map.of("type", "string", "description", "学生姓名"),
                    "age", Map.of("type", "integer", "description", "学生年龄"),
                    "teacherId", Map.of("type", "string", "description", "教师ID"),
                    "clazz", Map.of("type", "string", "description", "班级")
                ),
                "required", List.of("name", "age")
            )
        ));
        
        // 教师管理函数
        availableFunctions.put("get_teacher_by_id", Map.of(
            "description", "根据ID获取教师信息",
            "parameters", Map.of(
                "type", "object",
                "properties", Map.of(
                    "id", Map.of("type", "string", "description", "教师ID")
                ),
                "required", List.of("id")
            )
        ));
        
        availableFunctions.put("get_all_teachers", Map.of(
            "description", "获取所有教师信息",
            "parameters", Map.of("type", "object", "properties", Map.of())
        ));
        
        return availableFunctions;
    }
    
    @Override
    public void registerFunction(String functionName, Object functionHandler) {
        if (functionHandler instanceof Function) {
            functions.put(functionName, (Function<Map<String, Object>, Object>) functionHandler);
        } else {
            throw new IllegalArgumentException("函数处理器必须是Function类型");
        }
    }
}