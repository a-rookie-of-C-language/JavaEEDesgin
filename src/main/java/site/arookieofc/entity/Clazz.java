package site.arookieofc.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import site.arookieofc.annotation.validation.NotNullAndEmpty;

@Data
@NoArgsConstructor
public class Clazz {
    @NotNullAndEmpty
    private String id;
    
    @NotNullAndEmpty
    private String name;
    
    @NotNullAndEmpty
    private String teacherId;
    private Integer studentCount;
}