package site.arookieofc.entity;

import lombok.Data;
import site.arookieofc.pojo.dto.StudentDTO;

@Data
public class Student {
    private Integer id;
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

    public static Student fromDTO(StudentDTO dto) {
        if (dto == null) {
            return null;
        }
        Student student = new Student();
        student.setId(dto.getId());
        student.setName(dto.getName());
        student.setAge(dto.getAge());
        student.setTeacherId(dto.getTeacherId());
        student.setClazz(dto.getClazz());
        return student;
    }

    public void updateFromDTO(StudentDTO dto) {
        if (dto == null) {
            return;
        }
        if (dto.getName() != null) {
            this.name = dto.getName();
        }
        if (dto.getAge() != null) {
            this.age = dto.getAge();
        }
        if (dto.getTeacherId() != null) {
            this.teacherId = dto.getTeacherId();
        }
        if (dto.getClazz() != null) {
            this.clazz = dto.getClazz();
        }
    }
}
