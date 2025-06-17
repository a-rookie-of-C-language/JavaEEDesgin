package site.arookieofc.pojo.DO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.arookieofc.annotation.validation.Need;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Clazz {
    @Need
    private String id;
    
    @Need
    private String name;
    
    @Need
    private String teacherId;
    private Integer studentCount;
}