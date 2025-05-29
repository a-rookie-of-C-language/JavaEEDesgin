package site.arookieofc.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Clazz {
    private String id;
    private String name;
    private String teacherId;
    private Integer studentCount;
    private String description;
}