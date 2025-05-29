package site.arookieofc.entity;

import lombok.Data;

@Data
public class Teacher {
    private String id;
    private String name;
    private String department;  // 所属部门
    private String phone;
    private String email;

    public Teacher(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
