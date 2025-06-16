package site.arookieofc.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import site.arookieofc.pojo.dto.StudentDTO;

/**
 * 学生视图对象 - 用于Controller层与前端交互
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class StudentVO {
    private String id;
    private String name;
    private Integer age;
    private String teacherId;
    private String clazzId;
    private String teacherName;
    private String clazzName;
    
    /**
     * 转换为DTO
     */
    public StudentDTO toDTO() {
        StudentDTO dto = new StudentDTO();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setAge(this.age);
        dto.setTeacherId(this.teacherId);
        dto.setClazzId(this.clazzId);
        dto.setTeacherName(this.teacherName);
        dto.setClazzName(this.clazzName);
        return dto;
    }
    
    /**
     * 从DTO创建VO
     */
    public static StudentVO fromDTO(StudentDTO dto) {
        if (dto == null) {
            return null;
        }
        StudentVO vo = new StudentVO();
        vo.setId(dto.getId());
        vo.setName(dto.getName());
        vo.setAge(dto.getAge());
        vo.setTeacherId(dto.getTeacherId());
        vo.setClazzId(dto.getClazzId());
        vo.setTeacherName(dto.getTeacherName());
        vo.setClazzName(dto.getClazzName());
        return vo;
    }
    
    /**
     * 验证VO数据
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty()
                && age != null && age > 0 && age <= 150;
    }
    
    /**
     * 获取验证错误信息
     */
    public String getValidationError() {
        if (name == null || name.trim().isEmpty()) {
            return "学生姓名不能为空";
        }
        if (age == null) {
            return "学生年龄不能为空";
        }
        if (age <= 0 || age > 150) {
            return "学生年龄必须在1-150之间";
        }
        return null;
    }
}