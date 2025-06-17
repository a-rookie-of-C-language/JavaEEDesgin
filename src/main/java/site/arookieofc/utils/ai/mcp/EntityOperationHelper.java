package site.arookieofc.utils.ai.mcp;

import site.arookieofc.pojo.DO.Clazz;
import site.arookieofc.pojo.DO.Student;
import site.arookieofc.pojo.DO.Teacher;
import java.util.List;
import java.util.Map;

public class EntityOperationHelper {

    public static Student createStudentFromParams(Map<String, Object> params) {
        Student student = new Student();
        student.setId((String) params.get("id"));
        student.setName((String) params.get("name"));
        
        Object age = params.get("age");
        if (age instanceof Number) {
            student.setAge(((Number) age).intValue());
        }
        
        student.setTeacherId((String) params.get("teacherId"));
        student.setClazzId((String) params.get("clazzId"));
        return student;
    }

    public static void updateStudentFromParams(Student student, Map<String, Object> params) {
        if (params.containsKey("name")) {
            student.setName((String) params.get("name"));
        }
        if (params.containsKey("age")) {
            Object age = params.get("age");
            if (age instanceof Number) {
                student.setAge(((Number) age).intValue());
            }
        }
        if (params.containsKey("teacherId")) {
            student.setTeacherId((String) params.get("teacherId"));
        }
        if (params.containsKey("clazzId")) {
            student.setClazzId((String) params.get("clazzId"));
        }
    }

    public static Teacher createTeacherFromParams(Map<String, Object> params) {
        Teacher teacher = new Teacher();
        teacher.setId((String) params.get("id"));
        teacher.setName((String) params.get("name"));
        return teacher;
    }

    public static Clazz createClazzFromParams(Map<String, Object> params) {
        Clazz clazz = new Clazz();
        clazz.setId((String) params.get("id"));
        clazz.setName((String) params.get("name"));
        clazz.setTeacherId((String) params.get("teacherId"));
        return clazz;
    }

    public static void updateClazzFromParams(Clazz clazz, Map<String, Object> params) {
        if (params.containsKey("name")) {
            clazz.setName((String) params.get("name"));
        }
        if (params.containsKey("teacherId")) {
            clazz.setTeacherId((String) params.get("teacherId"));
        }
    }

    public static String formatStudent(Student student) {
        if (student == null) {
            return "学生不存在";
        }
        return String.format("ID: %s, 姓名: %s, 年龄: %d, 教师ID: %s, 班级ID: %s",
                student.getId(), student.getName(), student.getAge(),
                student.getTeacherId(), student.getClazzId());
    }

    public static String formatStudentList(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return "没有找到学生信息";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("学生列表 (共").append(students.size()).append("人):\n");
        for (Student student : students) {
            sb.append(formatStudent(student)).append("\n");
        }
        return sb.toString();
    }

    public static String formatTeacher(Teacher teacher) {
        if (teacher == null) {
            return "教师不存在";
        }
        return String.format("ID: %s, 姓名: %s", teacher.getId(), teacher.getName());
    }

    public static String formatTeacherList(List<Teacher> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            return "没有找到教师信息";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("教师列表 (共").append(teachers.size()).append("人):\n");
        for (Teacher teacher : teachers) {
            sb.append(formatTeacher(teacher)).append("\n");
        }
        return sb.toString();
    }

    public static String formatClazz(Clazz clazz) {
        if (clazz == null) {
            return "班级不存在";
        }
        return String.format("ID: %s, 班级名: %s, 教师ID: %s, 学生数: %d",
                clazz.getId(), clazz.getName(), clazz.getTeacherId(),
                clazz.getStudentCount() != null ? clazz.getStudentCount() : 0);
    }

    public static String formatClazzList(List<Clazz> clazzes) {
        if (clazzes == null || clazzes.isEmpty()) {
            return "没有找到班级信息";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("班级列表 (共").append(clazzes.size()).append("个):\n");
        for (Clazz clazz : clazzes) {
            sb.append(formatClazz(clazz)).append("\n");
        }
        return sb.toString();
    }
}