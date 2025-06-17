package site.arookieofc.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import site.arookieofc.pojo.dto.StudentDTO;

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
}