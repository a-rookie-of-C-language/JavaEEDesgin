package site.arookieofc.pojo.DO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.arookieofc.annotation.validation.Need;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Data
public class Teacher {
    private String id;
    
    @Need
    private String name;

    // Getter methods
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
