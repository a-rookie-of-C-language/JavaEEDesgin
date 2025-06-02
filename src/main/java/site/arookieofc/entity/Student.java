package site.arookieofc.entity;

import lombok.Data;
import site.arookieofc.pojo.dto.StudentDTO;

@Data
public class Student {
    private String id;
    private String name;
    private Integer age;
    private String teacherId;
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
