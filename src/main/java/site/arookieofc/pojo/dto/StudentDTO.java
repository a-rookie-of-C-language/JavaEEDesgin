package site.arookieofc.pojo.dto;

import lombok.Data;

@Data
public class StudentDTO {
    private String id;
    private String name;
    private Integer age;
    private String teacherId;
    private String clazz;
    private String teacherName; // 仅用于数据传输，不对应数据库字段
    
    /**
     * 转换为实体
     */
    public site.arookieofc.entity.Student toEntity() {
        site.arookieofc.entity.Student student = new site.arookieofc.entity.Student();
        student.setId(this.id);
        student.setName(this.name);
        student.setAge(this.age);
        student.setTeacherId(this.teacherId);
        student.setClazz(this.clazz);
        return student;
    }
    
    /**
     * 从实体创建DTO
     */
    public static StudentDTO fromEntity(site.arookieofc.entity.Student student) {
        if (student == null) {
            return null;
        }
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setAge(student.getAge());
        dto.setTeacherId(student.getTeacherId());
        dto.setClazz(student.getClazz());
        return dto;
    }
    
    /**
     * 验证DTO数据
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
