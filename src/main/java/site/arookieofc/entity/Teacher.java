package site.arookieofc.entity;

import lombok.Data;

@Data
public class Teacher {
    private String id;          // 教师编号
    private String name;        // 教师姓名
    private String department;  // 所属部门
    private String phone;       // 联系电话
    private String email;       // 邮箱
    private String subject;     // 任教科目
    
    public Teacher() {}
    
    public Teacher(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Teacher(String id, String name, String department) {
        this.id = id;
        this.name = name;
        this.department = department;
    }
}
