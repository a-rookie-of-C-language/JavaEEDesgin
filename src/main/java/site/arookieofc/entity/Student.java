package site.arookieofc.entity;

import lombok.Data;

@Data
public class Student {
    private int id;
    private String name;
    private int age;
    private String teacherId;
    private String clazz;
}
