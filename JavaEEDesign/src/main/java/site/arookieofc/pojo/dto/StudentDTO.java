package site.arookieofc.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import site.arookieofc.pojo.DO.Student;

/**
 * 学生数据传输对象 - 用于三层架构之间数据交换
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentDTO {
    private String id;
    private String name;
    private Integer age;
    private String teacherId;
    private String clazzId;
    private String teacherName;
    private String clazzName;
    
    /**
     * 转换为实体
     */
    public Student toEntity() {
        Student student = new Student();
        student.setId(this.id);
        student.setName(this.name);
        student.setAge(this.age);
        student.setTeacherId(this.teacherId);
        student.setClazzId(this.clazzId);
        return student;
    }
    
    /**
     * 从实体创建DTO
     */
    public static StudentDTO fromEntity(Student student) {
        if (student == null) {
            return null;
        }
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setAge(student.getAge());
        dto.setTeacherId(student.getTeacherId());
        dto.setClazzId(student.getClazzId());
        return dto;
    }
}
