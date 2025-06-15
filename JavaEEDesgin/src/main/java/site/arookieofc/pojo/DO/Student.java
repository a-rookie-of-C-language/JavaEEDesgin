package site.arookieofc.pojo.DO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import site.arookieofc.annotation.validation.Need;
import site.arookieofc.pojo.dto.StudentDTO;
import site.arookieofc.annotation.validation.Range;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ToString
public class Student {
    private String id;
    
    @Need
    private String name;
    
    @Range(min = 1, max = 150, message = "年龄必须在1-150之间")
    private Integer age;

    @Need
    private String teacherId;
    
    @Need
    private String clazzId;

    public StudentDTO toDTO() {
        StudentDTO dto = new StudentDTO();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setAge(this.age);
        dto.setTeacherId(this.teacherId);
        dto.setClazzId(this.clazzId);
        return dto;
    }
}
