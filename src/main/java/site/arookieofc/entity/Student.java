package site.arookieofc.entity;

import lombok.Data;
import site.arookieofc.pojo.dto.StudentDTO;
import site.arookieofc.annotation.validation.NotNullAndEmpty;
import site.arookieofc.annotation.validation.Range;

@Data
public class Student {
    private String id;
    
    @NotNullAndEmpty
    private String name;
    
    @Range(min = 1, max = 150, message = "年龄必须在1-150之间")
    private Integer age;

    @NotNullAndEmpty
    private String teacherId;
    
    @NotNullAndEmpty
    private String clazz;

    public StudentDTO toDTO() {
        StudentDTO dto = new StudentDTO();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setAge(this.age);
        dto.setTeacherId(this.teacherId);
        dto.setClazz(this.clazz);
        return dto;
    }
}
