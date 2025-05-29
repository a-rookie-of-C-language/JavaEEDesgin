package site.arookieofc.entity;

import lombok.Data;

@Data
public class Class {
    private String id;
    private String name;
    private String teacherId;
    private Integer studentCount;
    private String description;
}