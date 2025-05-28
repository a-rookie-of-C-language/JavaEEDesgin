package site.arookieofc.entity;

import lombok.Data;

@Data
public class Class {
    private String id;           // 班级编号
    private String name;         // 班级名称
    private String teacherId;    // 班主任教师ID
    private Integer studentCount; // 学生人数
    private String description;  // 班级描述
    
    public Class() {}
    
    public Class(String id, String name, String teacherId) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
    }
}