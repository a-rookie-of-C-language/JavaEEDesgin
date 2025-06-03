package site.arookieofc.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import site.arookieofc.annotation.validation.NotNullAndEmpty;

@NoArgsConstructor
@Data
public class Teacher {
    private String id;
    
    @NotNullAndEmpty
    private String name;
}
