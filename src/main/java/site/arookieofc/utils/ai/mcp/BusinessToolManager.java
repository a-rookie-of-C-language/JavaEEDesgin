package site.arookieofc.utils.ai.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import site.arookieofc.pojo.DO.Student;
import site.arookieofc.pojo.DO.Teacher;
import site.arookieofc.pojo.DO.Clazz;
import site.arookieofc.processor.ioc.ApplicationContextHolder;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;
import site.arookieofc.service.ClazzService;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;

public class BusinessToolManager {

    private static volatile BusinessToolManager instance;
    private StudentService studentService;
    private TeacherService teacherService;
    private ClazzService clazzService;

    public static BusinessToolManager getInstance() {
        if (instance == null) {
            synchronized (BusinessToolManager.class) {
                if (instance == null) {
                    instance = new BusinessToolManager();
                    instance.init();
                }
            }
        }
        return instance;
    }

    private void init() {
        this.studentService = ApplicationContextHolder.getBean(StudentService.class);
        this.teacherService = ApplicationContextHolder.getBean(TeacherService.class);
        this.clazzService = ApplicationContextHolder.getBean(ClazzService.class);
    }

    // ==================== 工具创建方法 ====================

    public McpServerFeatures.SyncToolSpecification createStudentQueryTool() {
        return McpToolBuilder.createToolSpecification(
                "queryStudent", "查询学生信息", "student_query",
                (exchange, args) -> handleStudentQueryTool((String) args.get("operation"), args),
                "查询学生失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createStudentAddTool() {
        return McpToolBuilder.createToolSpecification(
                "addStudent", "添加学生", "student_add",
                (exchange, args) -> handleAddStudentTool(args),
                "添加学生失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createStudentUpdateTool() {
        return McpToolBuilder.createToolSpecification(
                "updateStudent", "更新学生信息", "student_update",
                (exchange, args) -> handleUpdateStudentTool(args),
                "更新学生失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createStudentDeleteTool() {
        return McpToolBuilder.createDeleteToolSpecification(
                "deleteStudent", "删除学生",
                studentService::deleteStudent,
                "成功删除学生", "删除学生失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createTeacherQueryTool() {
        return McpToolBuilder.createToolSpecification(
                "queryTeacher", "查询教师信息", "teacher_query",
                (exchange, args) -> handleTeacherQueryTool((String) args.get("operation"), args),
                "查询教师失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createTeacherAddTool() {
        return McpToolBuilder.createToolSpecification(
                "addTeacher", "添加教师", "teacher_add",
                (exchange, args) -> handleAddTeacherTool(args),
                "添加教师失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createTeacherUpdateTool() {
        return McpToolBuilder.createToolSpecification(
                "updateTeacher", "更新教师信息", "teacher_update",
                (exchange, args) -> handleUpdateTeacherTool(args),
                "更新教师失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createTeacherDeleteTool() {
        return McpToolBuilder.createDeleteToolSpecification(
                "deleteTeacher", "删除教师",
                teacherService::deleteTeacher,
                "成功删除教师", "删除教师失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createClazzQueryTool() {
        return McpToolBuilder.createToolSpecification(
                "queryClazz", "查询班级信息", "clazz_query",
                (exchange, args) -> handleClazzQueryTool((String) args.get("operation"), args),
                "查询班级失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createClazzAddTool() {
        return McpToolBuilder.createToolSpecification(
                "addClazz", "添加班级", "clazz_add",
                (exchange, args) -> handleAddClazzTool(args),
                "添加班级失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createClazzUpdateTool() {
        return McpToolBuilder.createToolSpecification(
                "updateClazz", "更新班级信息", "clazz_update",
                (exchange, args) -> handleUpdateClazzTool(args),
                "更新班级失败"
        );
    }

    public McpServerFeatures.SyncToolSpecification createClazzDeleteTool() {
        return McpToolBuilder.createDeleteToolSpecification(
                "deleteClazz", "删除班级",
                clazzService::deleteClass,
                "成功删除班级", "删除班级失败"
        );
    }

    // ==================== 处理方法（改为实例方法）====================

    private String handleStudentQueryTool(String operation, Map<String, Object> args) {
        return switch (operation) {
            case "getAll" -> {
                List<Student> students = studentService.getAllStudents();
                yield EntityOperationHelper.formatStudentList(students);
            }
            case "getById" -> {
                String id = (String) args.get("id");
                Student student = studentService.getStudentById(id);
                yield EntityOperationHelper.formatStudent(student);
            }
            case "getByClass" -> {
                String clazzId = (String) args.get("clazzId");
                List<Student> students = studentService.getStudentsByClass(clazzId);
                yield EntityOperationHelper.formatStudentList(students);
            }
            case "getByTeacher" -> {
                String teacherId = (String) args.get("teacherId");
                List<Student> students = studentService.getStudentsByTeacher(teacherId);
                yield EntityOperationHelper.formatStudentList(students);
            }
            default -> "不支持的查询操作: " + operation;
        };
    }

    private String handleAddStudentTool(Map<String, Object> args) {
        // 处理可能的编码问题
        Map<String, Object> processedArgs = processEncodingIssues(args);

        Student student = EntityOperationHelper.createStudentFromParams(processedArgs);
        studentService.addStudent(student);
        return "成功添加学生: " + student.getName() + " (ID: " + student.getId() + ")";
    }

    private String handleUpdateStudentTool(Map<String, Object> args) {
        // 处理可能的编码问题
        Map<String, Object> processedArgs = processEncodingIssues(args);

        String id = (String) processedArgs.get("id");
        Student student = studentService.getStudentById(id);
        EntityOperationHelper.updateStudentFromParams(student, processedArgs);
        studentService.updateStudent(student);
        return "成功更新学生信息: " + student.getName() + " (ID: " + id + ")";
    }

    private String handleTeacherQueryTool(String operation, Map<String, Object> args) {
        return switch (operation) {
            case "getAll" -> {
                List<Teacher> teachers = teacherService.getAllTeachers();
                yield EntityOperationHelper.formatTeacherList(teachers);
            }
            case "getById" -> {
                String id = (String) args.get("id");
                Teacher teacher = teacherService.getTeacherById(id);
                yield EntityOperationHelper.formatTeacher(teacher);
            }
            default -> "不支持的查询操作: " + operation;
        };
    }

    private String handleAddTeacherTool(Map<String, Object> args) {
        // 处理可能的编码问题
        Map<String, Object> processedArgs = processEncodingIssues(args);

        Teacher teacher = EntityOperationHelper.createTeacherFromParams(processedArgs);
        teacherService.addTeacher(teacher);
        return "成功添加教师: " + teacher.getName() + " (ID: " + teacher.getId() + ")";
    }

    private String handleUpdateTeacherTool(Map<String, Object> args) {
        // 处理可能的编码问题
        Map<String, Object> processedArgs = processEncodingIssues(args);

        Teacher teacher = EntityOperationHelper.createTeacherFromParams(processedArgs);
        teacherService.updateTeacher(teacher);
        return "成功更新教师信息: " + teacher.getName() + " (ID: " + teacher.getId() + ")";
    }

    private String handleClazzQueryTool(String operation, Map<String, Object> args) {
        return switch (operation) {
            case "getAll" -> {
                List<Clazz> clazzes = clazzService.getAllClasses();
                yield EntityOperationHelper.formatClazzList(clazzes);
            }
            case "getById" -> {
                String id = (String) args.get("id");
                Clazz clazz = clazzService.getClassById(id);
                yield EntityOperationHelper.formatClazz(clazz);
            }
            case "getByTeacher" -> {
                String teacherId = (String) args.get("teacherId");
                List<Clazz> clazzes = clazzService.getClassesByTeacher(teacherId);
                yield EntityOperationHelper.formatClazzList(clazzes);
            }
            default -> "不支持的查询操作: " + operation;
        };
    }

    private String handleAddClazzTool(Map<String, Object> args) {
        // 处理可能的编码问题
        Map<String, Object> processedArgs = processEncodingIssues(args);

        Clazz clazz = EntityOperationHelper.createClazzFromParams(processedArgs);
        clazzService.addClass(clazz);
        return "成功添加班级: " + clazz.getName() + " (ID: " + clazz.getId() + ")";
    }

    private String handleUpdateClazzTool(Map<String, Object> args) {
        // 处理可能的编码问题
        Map<String, Object> processedArgs = processEncodingIssues(args);

        String id = (String) processedArgs.get("id");
        Clazz clazz = clazzService.getClassById(id);
        EntityOperationHelper.updateClazzFromParams(clazz, processedArgs);
        clazzService.updateClass(clazz);
        return "成功更新班级信息: " + clazz.getName() + " (ID: " + id + ")";
    }

    // 提取编码处理逻辑为独立方法
    private Map<String, Object> processEncodingIssues(Map<String, Object> args) {
        Map<String, Object> processedArgs = new HashMap<>();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String strValue) {
                try {
                    // 尝试修复可能的编码问题
                    byte[] bytes = strValue.getBytes(StandardCharsets.ISO_8859_1);
                    String corrected = new String(bytes, StandardCharsets.UTF_8);
                    processedArgs.put(entry.getKey(), corrected);
                } catch (Exception e) {
                    // 如果转换失败，使用原值
                    processedArgs.put(entry.getKey(), value);
                }
            } else {
                processedArgs.put(entry.getKey(), value);
            }
        }
        return processedArgs;
    }
}