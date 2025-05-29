package site.arookieofc.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Teacher {
    private String id;
    private String name;
    private String department;  // 所属部门
}
