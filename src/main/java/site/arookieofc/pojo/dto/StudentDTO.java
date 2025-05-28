package site.arookieofc.pojo.dto;

import lombok.Data;

@Data
public class StudentDTO {
    private int id;
    private String name;
    private int age;
    private String teacherId;
    private String clazz;
}
